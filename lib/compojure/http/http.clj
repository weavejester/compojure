;; compojure.http -- HTTP resource library for Compojure

(clojure/in-ns 'compojure.http)
(clojure/refer 'clojure)
(clojure/refer 'clojure.contrib.lib)

(use '(compojure str-utils file-utils))

(import '(java.io File FileInputStream)
        '(javax.servlet ServletContext)
        '(javax.servlet.http HttpServlet
                             HttpServletRequest
                             HttpServletResponse))

;;;; Mimetypes ;;;;

(defn context-mimetype
  "Guess the mimetype of a filename. Defaults to 'application/octet-stream'
  if the mimetype is unknown."
  [#^ServletContext context filename]
  (or (.getMimeType context filename)
      "application/octet-stream"))

;;;; Routes ;;;;

(defn- part->regex
  [[value match?]]
  (if match?
    (if (= value "*")
      "(.*?)"
      "([^/.,;?]+)")
    (re-escape value)))

(defn- part->keyword
  [[value _]]
  (if (= value "*")
    :*
    (keyword (.substring value 1))))
  
(defn parse-route
  "Turn a route string into a regex and seq of symbols."
  [route]
  (let [parts (re-parts #":\\w+|\\*" route)]
    [(re-pattern
       (str-map part->regex parts))
     (map part->keyword
          (filter second parts))]))

(defn match-route 
  "Match a path against a parsed route. Returns a map of keywords and their
  matching path values."
  [[regex symbols] path]
  (let [matcher (re-matcher regex path)]
    (if (. matcher (matches))
      (reduce
        (fn [map [key val]]
          (if-let cur (map key)
            (if (vector? cur)
              (assoc map key (conj cur val))
              (assoc map key [cur val]))
            (assoc map key val)))
        {}
        (map vector symbols (rest (re-groups matcher)))))))

(def #^{:doc
  "A global list of all registered resources. A resource is a vector
  consisting of a HTTP method, a parsed route and function that takes in
  a context, request and response object.
  e.g.
  [\"GET\"
   (parse-route \"/welcome/:name\") 
   (fn [context request response] ...)]"}
  *resources* (ref '()))

(defn assoc-route
  "Associate a HTTP method and route with a resource function."
  [method route resource]
  (dosync
    (commute *resources* conj
      [method (parse-route route) resource])))

;;;; Response ;;;;

(defn update-response
  "Destructively update a HttpServletResponse using a Clojure datatype:
    string - Adds to the response body
    seq    - Adds all containing elements to the response body
    map    - Updates the HTTP headers
    Number - Updates the status code
    File   - Updates the response body via a file stream
    vector - Iterates through its contents, successively updating the response
             with each value"
   [#^HttpServletResponse response context update]
   (cond 
     (vector? update)
       (doseq u update
         (update-response response context u))
     (string? update)
       (.. response (getWriter) (print update))
     (seq? update)
       (let [writer (.getWriter response)]
         (doseq d update
           (.print writer d)))
     (map? update)
       (doseq [k v] update
         (.setHeader response k v))
     (instance? Number update)
       (.setStatus response update)
     (instance? File update)
       (let [out (.getOutputStream response)
             in  (new FileInputStream update)]
         (.setHeader response
            "Content-Type" (context-mimetype context (str update)))
       (pipe-stream in out))))

(defn redirect-to
  "A shortcut for a '302 Moved' HTTP redirect."
  [location]
  [302 {"Location" location}])

(defn not-found
  "A shortcut to create a '404 Not Found' HTTP response."
  ([] (not-found "public/404.html"))
  ([filename] [404 (file filename)]))

;;;; Resource ;;;;

(defn get-session
  "Returns a ref to a hash-map that acts as a HTTP session that can be updated
  within a Clojure STM transaction."
  [#^HttpServletRequest request]
  (let [session (.getSession request)]
    (or (.getAttribute session "clj-session")
        (let [clj-session (ref {})]
          (.setAttribute session "clj-session" clj-session)
          clj-session))))

(defmacro http-resource
  "Macro that wraps the body of a resource up in a standalone function."
  [& body]
  `(fn ~'[route context request]
     (let ~'[method    (.getMethod    request)
             full-path (.getPathInfo  request)
             param    #(.getParameter request (compojure.str-utils/str* %))
             header   #(.getHeader    request (compojure.str-utils/str* %))
             mimetype #(compojure.http/context-mimetype (str %))
             session   (compojure.http/get-session request)]
       (do ~@body))))

(defn serve-file
  "Serve up a static file from a directory. A 404 is returned if the file
  does not exist."
  ([path]
    (serve-file "public" path))
  ([root path]
    (let [static-file (file root path)]
      (if (.isFile static-file)
        static-file
        :next))))

(defn find-method
  "Returns either the value of the '_method' HTTP parameter, or the method
  of the HTTP request."
  [#^HttpServletRequest request]
  (or (.getParameter request "_method")
      (.getMethod request)))

(defn apply-http-resource
  "Finds and evaluates the resource that matches the HttpServletRequest. If the
  resource returns :next, the next matching resource is evaluated."
  [context request response]
  (let [method    (find-method request)
        path      (.getPathInfo request)
        route?    (fn [meth route]
                    (if (or (nil? meth) (= meth method))
                      (match-route route path)))
        response? (fn [[meth route resource]]
                    (if-let route-params (route? meth route)
                      (let [resp (resource route-params
                                           context
                                           request)]
                        (if (not= :next resp)
                          (or resp [])))))]
    (update-response response context
      (some response? @*resources*))))
            
(defmacro GET "Creates a GET resource."
  [route & body]
  `(assoc-route "GET" ~route (http-resource ~@body)))

(defmacro PUT "Creates a PUT resource."
  [route & body]
  `(assoc-route "PUT" ~route (http-resource ~@body)))

(defmacro POST "Creates a POST resource."
  [route & body]
  `(assoc-route "POST" ~route (http-resource ~@body)))

(defmacro DELETE "Creates a DELETE resource."
  [route & body]
  `(assoc-route "DELETE" ~route (http-resource ~@body)))

(defmacro HEAD "Creates a HEAD resource."
  [route & body]
  `(assoc-route "HEAD" ~route (http-resource ~@body)))

(defmacro ANY "Creates a resource that responds to any HTTP method."
  [route & body]
  `(assoc-route nil ~route (http-resource ~@body)))

(defn new-servlet
  "Create a new servlet from a function that takes three arguments of types
  HttpServletContext, HttpServletRequest, HttpServletResponse."
  [func] 
  (proxy [HttpServlet] []
    (service [request response]
      (func (.getServletContext this) request response))))

(def #^{:doc "A servlet that handles all the defined resources."}
  resource-servlet
  (new-servlet apply-http-resource))
