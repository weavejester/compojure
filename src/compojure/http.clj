;; compojure.http
;;
;; Compojure library for constructing HTTP servlet proxy objects. 
;; 
;; Here's a small taste of the syntax:
;;
;;   (defservlet example
;;     "An example servlet that contains a bit of everything."
;;     (GET "/"
;;       "Hello World")
;;     (GET "/:name"
;;       (str "Hello " (route :name)))
;;     (GET "/image"
;;       (file "public/image.png"))
;;     (GET "/error"
;;       [500 "Error 500"])
;;     (POST "/mesg"
;;       (redirect-to "/"))
;;     (GET "/header"
;;       [{"X-Fortune" "Be prepared!"}
;;        "Custom X-Header"])
;;     (PUT "/:var"
;;       (dosync
;;         (commute session assoc :var (route :var)))
;;     (ANY "/*"
;;       (page-not-found))

(ns compojure.http
  (:use (compojure control
                   file-utils
                   parser
                   str-utils))
  (:import (java.io File
                    InputStream
                    FileInputStream)
           (java.net URL)
           (java.util Enumeration
                      Map$Entry)
           (java.util.regex Pattern)
           (javax.servlet ServletContext)
           (javax.servlet.http Cookie
                               HttpServlet
                               HttpServletRequest
                               HttpServletResponse)))
;;;; Mimetypes ;;;;
 
(defn context-mimetype
  "Guess the mimetype of a filename. Defaults to 'application/octet-stream'
  if the mimetype is unknown."
  [#^ServletContext context filename]
  (or (.getMimeType context filename)
      "application/octet-stream"))
 
;;;; Routes ;;;;
 
(defstruct url-route
  :regex
  :keywords)

(defmulti compile-route class)

(defmethod compile-route String
  [route]
  (let [splat #"\*"
        word  #":(\w+)"
        path  #"[^:*]+"]
    (struct url-route
      (re-pattern
        (apply str
          (parse route
            splat "(.*?)"
            word  "([^/.,;?]+)"
            path  #(re-escape (.group %)))))
      (filter (complement nil?)
        (parse route
          splat :*
          word  #(keyword (.group % 1))
          path  nil)))))

(defmethod compile-route Pattern
  [route]
  route)

(defn- match-group-map
  "Create a hash-map from a series of regex match groups and a collection of
  keywords."
  [groups keywords]
  (reduce
    (partial merge-with
      #(conj (ifn vector? vector %1) %2))
    {}
    (map hash-map keywords (rest groups))))

(defn match-route
  "Match a path against a parsed route. Returns a map of keywords and their
  matching path values."
  [route path]
  (if (instance? Pattern route)
    (let [matcher (re-matcher route path)]
      (if (.matches matcher)
        (vec (rest (re-groups matcher)))))
    (let [matcher (re-matcher (route :regex) path)]
      (if (.matches matcher)
        (match-group-map (re-groups matcher) (route :keywords))))))

;;;; Handler functions ;;;;
 
(defstruct http-handler
  :method
  :route
  :function)

(defn- parse-key-value
  "Parse a key and value to make them more Clojure-friendly."
  [key val]
  [(keyword key)
   (if (rest val) val (first val))])

(defn get-params
  "Creates a name/value map of all the request parameters."
  [#^HttpServletRequest request]
  (apply hash-map
    (mapcat 
      (fn [#^Map$Entry e] (parse-key-value (.getKey e) (.getValue e)))
      (.getParameterMap request))))

(defn get-headers
  "Creates a name/value map of all the request headers."
  [#^HttpServletRequest request]
  (apply hash-map
    (mapcat
      #(parse-key-value (.toLowerCase %)
                        (enumeration-seq (.getHeaders request %)))
       (enumeration-seq (.getHeaderNames request)))))

(defn get-session
  "Returns a ref to a hash-map that acts as a HTTP session that can be updated
  within a Clojure STM transaction."
  [#^HttpServletRequest request]
  (let [session (.getSession request)]
    (or (.getAttribute session "clj-session")
        (let [clj-session (ref {})]
          (.setAttribute session "clj-session" clj-session)
          clj-session))))

(defn get-cookies
  "Creates a name/value map from all of the cookies in the request."
  [#^HttpServletRequest request]
  (apply hash-map
    (mapcat #(list (keyword (.getName %)) (.getValue %))
             (.getCookies request))))

(defmacro handler-fn
  "Macro that wraps the body of a handler up in a standalone function."
  [& body]
  `(fn ~'[route context request]
     (let ~'[method    (.getMethod    request)
             full-path (.getPathInfo  request)
             params    (compojure.http/get-params  request)
             headers   (compojure.http/get-headers request)
             mimetype #(compojure.http/context-mimetype context (str %))
             session   (compojure.http/get-session request)
             cookies   (compojure.http/get-cookies request)]
       (do ~@body))))
 
(defn- find-method
  "Returns either the value of the '_method' HTTP parameter, or the method
  of the HTTP request."
  [#^HttpServletRequest request]
  (or (.getParameter request "_method")
      (.getMethod request)))

(defn- set-type-by-name
  "Set the content type header by guessing the mimetype from the resource name."
  [#^HttpServletResponse response context name]
  (.setHeader response "Content-Type" (context-mimetype context name)))

(defn- update-response
  "Destructively update a HttpServletResponse using a Clojure datatype:
    string      - Adds to the response body
    seq         - Adds all containing elements to the response body
    map         - Updates the HTTP headers
    Number      - Updates the status code
    File        - Updates the response body via a file stream
    URL         - Updates the response body via a stream to the URL
    InputStream - Pipes the input stream to the resource body
    vector      - Iterates through its contents, successively updating the
                  response with each value"
  [#^HttpServletResponse response context update]
  (cond
    (vector? update)
      (doseq [u update]
        (update-response response context u))
    (string? update)
      (.. response (getWriter) (print update))
    (seq? update)
      (let [writer (.getWriter response)]
        (doseq [d update]
          (.print writer d)))
    (map? update)
      (doseq [[k v] update]
        (.setHeader response k v))
    (instance? Number update)
      (.setStatus response update)
    (instance? Cookie update)
      (.addCookie response update)
    (instance? File update)
      (update-response response context (.toURL update))
    (instance? URL update)
      (do (set-type-by-name response context (str update))
          (update-response response context (.openStream update)))
    (instance? InputStream update)
      (with-open [in update]
        (pipe-stream in (.getOutputStream response)))))
        
(defn- apply-http-handler
  "Finds and evaluates the handler that matches the HttpServletRequest. If the
  handler returns :next, the next matching handler is evaluated."
  [handlers context request response]
  (let [method (find-method request)
        path     (.getPathInfo request)
        method= #(or (nil? %) (= method %))
        route?   (fn [handler]
                   (if (method= (handler :method))
                     (match-route (handler :route) path)))
        response? (fn [handler]
                    (if-let [route-params (route? handler)]
                      (let [func (handler :function)
                            resp (func route-params
                                       context
                                       request)]
                        (if (not= :next resp)
                          (or resp [])))))]
    (update-response response context
      (some response? handlers))))

;;;; Public macros ;;;;

(defmacro GET "Creates a GET handler."
  [route & body]
  `(struct http-handler "GET" (compile-route ~route) (handler-fn ~@body)))
 
(defmacro PUT "Creates a PUT handler."
  [route & body]
  `(struct http-handler "PUT" (compile-route ~route) (handler-fn ~@body)))
 
(defmacro POST "Creates a POST handler."
  [route & body]
  `(struct http-handler "POST" (compile-route ~route) (handler-fn ~@body)))
 
(defmacro DELETE "Creates a DELETE handler."
  [route & body]
  `(struct http-handler "DELETE" (compile-route ~route) (handler-fn ~@body)))
 
(defmacro HEAD "Creates a HEAD handler."
  [route & body]
  `(struct http-handler "HEAD" (compile-route ~route) (handler-fn ~@body)))
 
(defmacro ANY "Creates a handler that responds to any HTTP method."
  [route & body]
  `(struct http-handler nil (compile-route ~route) (handler-fn ~@body)))

;;;; Helper functions ;;;;

(defn new-cookie
  "Create a new Cookie object."
  [name value & attrs]
  (let [cookie (new Cookie (str* name) value)
        attrs  (apply hash-map attrs)
        iff    (fn [attr func] (if attr (func attr)))]
    (iff (attrs :comment) #(.setComment cookie %))
    (iff (attrs :domain)  #(.setDomain  cookie %))
    (iff (attrs :max-age) #(.setMaxAge  cookie %))
    (iff (attrs :path)    #(.setPath    cookie %))
    (iff (attrs :secure)  #(.setSecure  cookie %))
    (iff (attrs :version) #(.setVersion cookie %))
    cookie))
 
(defn redirect-to
  "A shortcut for a '302 Moved' HTTP redirect."
  [location]
  [302 {"Location" location}])
 
(defn page-not-found
  "A shortcut to create a '404 Not Found' HTTP response."
  ([]         (page-not-found "public/404.html"))
  ([filename] [404 (file filename)]))
 
(defn- find-index-file
  "Search the directory for index.*"
  [dir]
  (first (filter
          #(re-matches #"index\\..*" (.toLowerCase (.getName %)))
           (.listFiles dir))))

(defn serve-file
  "Attempts to serve up a static file from a directory, which defaults to
  './public'. Nil is returned if the file does not exist. If the file is a
  directory, the function looks for a file in the directory called 'index.*'."
  ([path]
    (serve-file "public" path))
  ([root path]
    (let [filepath (file root path)]
      (cond
        (.isFile filepath)
          filepath
        (.isDirectory filepath)
          (find-index-file filepath)))))
 
;;;; Servlet creation ;;;;
 
(defn http-service
  "Represents the service method called by a HttpServlet."
  [#^HttpServlet this request response handlers]
  (.setCharacterEncoding response "UTF-8")
  (apply-http-handler handlers
                      (.getServletContext this)
                      request
                      response))
(defn servlet
  "Create a servlet from a sequence of handlers."
  [& handlers]
  (proxy [HttpServlet] []
    (service [request response]
      (http-service this request response handlers))))

(defn update-servlet
  "Update an existing servlet proxy with a new set of handlers."
  [object & handlers]
  (update-proxy object
    {"service" (fn [this request response]
                 (http-service this request response handlers))}))

(defmacro defservlet
  "Defines a new servlet with an optional doc-string, or if a servlet is
  already defined, it updates the existing servlet with the supplied handlers.
  Note that updating is not a thread-safe operation."
  [name doc & handlers]
  (if (string? doc)
    `(do (defonce
          ~(with-meta name (assoc (meta name) :doc doc))
           (proxy [HttpServlet] []))
         (update-servlet ~name ~@handlers))
    `(do (defonce ~name
           (proxy [HttpServlet] []))
         (update-servlet ~name ~doc ~@handlers))))

(defmacro defservice
  "Defines a service method with an optional prefix suitable for being used by
  genclass to compile a HttpServlet class.
  e.g. (defservice
         (GET \"/\" \"Hello World\"))
       (defservice \"myprefix-\"
         (GET \"/\" \"Hello World\"))"
  [prefix & handlers]
  (let [[prefix handlers] (if (string? prefix)
                           [prefix handlers]
                           ["-"   (cons prefix handlers)])]
    `(defn ~(symbol (str prefix "service"))
     ~'[this request response]
       (http-service ~'this ~'request ~'response (list ~@handlers)))))
