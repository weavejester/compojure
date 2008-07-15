;; http.clj -- HTTP resource library for Compojure

(clojure/in-ns 'http)
(clojure/refer 'clojure)

(lib/use compojure file-utils)

(import '(java.io File FileInputStream)
        '(javax.servlet.http HttpServletRequest HttpServletResponse))

;;;; Mimetypes ;;;;

(def *default-mimetype* "application/octet-stream")

(defn context-mimetype
  "Get the mimetype of a filename using the ServletContext."
  [context filename]
  (or (. context (getMimeType filename))
      *default-mimetype*))

;;;; Routes ;;;;

(defn parse-route
  "Turn a route string into a regex and seq of symbols."
  [route]
  (let [segment  "([^/.,;?]+)"
        matcher  (re-matcher #":([a-z_]+)" (re-escape route))
        symbols  (re-find-all matcher)
        regex    (. matcher (replaceAll segment))]
    [(re-pattern regex)
     (map (comp keyword second) symbols)]))

(defn match-route 
  "Match a path against a parsed route. Returns a map of keywords and their
  matching path values."
  [[regex symbols] path]
  (let [matcher (re-matcher regex path)]
    (if (. matcher (matches))
      (apply hash-map
        (interleave symbols (rest (re-groups matcher)))))))

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

(defn base-responder
  "Basic Compojure responder. Handles the following datatypes:
    string - Adds to the response body
    seq    - Adds all containing elements to the response body
    map    - Updates the HTTP headers
    Number - Updates the status code
    File   - Updates the response body via a file stream"
  [#^HttpServletResponse response context update]
  (cond 
    (string? update)
      (.. response (getWriter) (print update))
    (seq? update)
      (let [writer (. response (getWriter))]
        (doseq d update
          (. writer (print d))))
    (map? update)
      (doseq [k v] update
        (. response (setHeader k v)))
    (instance? Number update)
      (. response (setStatus update))
    (instance? File update)
      (let [out (. response (getOutputStream))
            in  (new FileInputStream update)]
        (. response (setHeader
          "Content-Type" (context-mimetype context (str update))))
        (pipe-stream in out))))

(def *responders*
  (ref (list base-responder)))

(defn add-resource-responder [func]
  (dosync
    (commute *responders* conj func)))

(defn update-response
  "Destructively update a HttpServletResponse via a Clojure datatype. Vectors
  can be used to string different values together."
  [#^HttpServletResponse response context update]
  (if (vector? update)
    (doseq d update
      (update-response response context d))
    (some #(% response context update) @*responders*)))

(defn redirect-to
  "A handy shortcut for a '302 Moved' HTTP redirect."
  [location]
  [302 {"Location" location}])

;;;; Resource ;;;;

(defn get-session
  "Pulls a Clojure-friendly session (a map reference) from a HttpRequest."
  [#^HttpServletRequest request]
  (let [session (. request (getSession))]
    (or (. session (getAttribute "clj-session"))
        (let [clj-session (ref {})]
          (. session (setAttribute "clj-session" clj-session))
          clj-session))))

(def #^{:doc
  "A set of bindings available to each resource. This can be extended
  by plugins, if required."}
  *resource-bindings*
  (ref '(method    (. request (getMethod))
         full-path (. request (getPathInfo))
         param    #(. request (getParameter (compojure/str* %)))
         header   #(. request (getHeader (compojure/str* %)))
         mimetype #(http/context-mimetype (str %))
         session   (http/get-session request))))

(defn add-resource-binding
  "Add a binding to the set of default bindings assigned to a resource."
  [name binding]
  (dosync
    (commute *resource-bindings* concat [name binding])))

(defmacro http-resource
  "Create a pseudo-servlet from a resource. It's not quite a real
  servlet because it's a function, rather than an HttpServlet object."
  [& body]
  `(fn ~'[route context request response]
     (let ~(apply vector @*resource-bindings*)
       (update-response ~'response ~'context (do ~@body)))))

(def *default-resource*
  (http-resource
    (let [static-file (file "public" full-path)]
      (if (. static-file (isFile))
        static-file
        [404 (file "public/404.html")]))))

(defn find-resource
  "Find the first resource that matches the HttpServletRequest"
  [#^HttpServletRequest request response]
  (let [method    (. request (getMethod))
        path      (. request (getPathInfo))
        matches?  (fn [[meth route resource]]
                    (if (= meth method)
                      (if-let route-params (match-route route path)
                        (partial resource route-params) nil)))]
    (or
      (some matches? @*resources*)
      (partial *default-resource* {}))))

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

(def #^{:doc "A servlet that handles all the defined resources."}
  resource-servlet
  (new-servlet
    (fn [context request response]
      (let [resource (find-resource request response)]
        (resource context request response)))))
