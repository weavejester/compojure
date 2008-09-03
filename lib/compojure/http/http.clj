;; HTTP resource library for Compojure
(ns compojure.http)

(use '(compojure control
                 file-utils
                 parser
                 str-utils))

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

(defstruct route
  :regex
  :keywords)

(defn compile-route
  "Turn a route string into a regex and seq of symbols."
  [route-str]
  (let [splat #"\\*"
        word  #":(\\w+)"
        path  #"[^:*]+"]
    (struct route
      (re-pattern
        (apply str
          (parse route-str
            splat "(.*?)"
            word  "([^/.,;?]+)"
            path  #(re-escape (.group %)))))
      (filter (complement nil?)
        (parse route-str
          splat :*
          word  #(keyword (.group % 1))
          path  nil)))))

(defn match-route 
  "Match a path against a parsed route. Returns a map of keywords and their
  matching path values."
  [route path]
  (let [matcher (re-matcher (route :regex) path)]
    (if (.matches matcher)
      (reduce
        (partial merge-with
          #(conj (ifn vector? vector %1) %2))
        {}
        (map hash-map
          (route :keywords)
          (rest (re-groups matcher)))))))

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

(defstruct http-action
  :method
  :route
  :function)

(defn get-session
  "Returns a ref to a hash-map that acts as a HTTP session that can be updated
  within a Clojure STM transaction."
  [#^HttpServletRequest request]
  (let [session (.getSession request)]
    (or (.getAttribute session "clj-session")
        (let [clj-session (ref {})]
          (.setAttribute session "clj-session" clj-session)
          clj-session))))

(defmacro action-fn
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

(defn apply-http-action
  "Finds and evaluates the action that matches the HttpServletRequest. If the
  action returns :next, the next matching action is evaluated."
  [actions context request response]
  (let [method    (find-method request)
        path      (.getPathInfo request)
        method=  #(or (nil? %) (= method %))

        route?    (fn [action]
                    (if (method= (action :method))
                      (match-route (action :route) path)))

        response? (fn [action]
                    (if-let route-params (route? action)
                      (let [func (action :function)
                            resp (func route-params
                                       context
                                       request)]
                        (if (not= :next resp)
                          (or resp [])))))]
    (update-response response context
      (some response? actions))))
            
(defmacro GET "Creates a GET resource."
  [route & body]
  `(struct http-action "GET" (compile-route ~route) (action-fn ~@body)))

(defmacro PUT "Creates a PUT resource."
  [route & body]
  `(struct http-action "PUT" (compile-route ~route) (action-fn ~@body)))

(defmacro POST "Creates a POST resource."
  [route & body]
  `(struct http-action "POST" (compile-route ~route) (action-fn ~@body)))

(defmacro DELETE "Creates a DELETE resource."
  [route & body]
  `(struct http-action "DELETE" (compile-route ~route) (action-fn ~@body)))

(defmacro HEAD "Creates a HEAD resource."
  [route & body]
  `(struct http-action "HEAD" (compile-route ~route) (action-fn ~@body)))

(defmacro ANY "Creates a resource that responds to any HTTP method."
  [route & body]
  `(struct http-action nil (compile-route ~route) (action-fn ~@body)))

;;;; Servlet ;;;;

(defn extend-resource
  [name & actions]
  (dosync
    (commute name #(concat actions %))))

(defmacro resource
  "Construct a new resource, or add to an existing one."
  [name doc & resources]
  `(do (defonce
        ~(if (string? doc)
           (with-meta name (assoc (meta name) :doc doc))
           name)
         (ref nil))
       (extend-resource ~name
         ~@(if (string? doc)
             resources
             (cons doc resources)))))


(defn servlet
  "Create a servlet from a list of resources."
  [resources]
  (proxy [HttpServlet] []
    (service [request response]
      (apply-http-action @resources
                         (.getServletContext this)
                         request
                         response))))
