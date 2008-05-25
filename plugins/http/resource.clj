(in-ns 'compojure-resource)
(clojure/refer 'clojure)
(clojure/refer 'compojure)

(import '(java.io FileInputStream))
(import '(javax.servlet.http HttpServletRequest HttpServletResponse))

;;;; Mimetypes ;;;;

(def *default-mimetype* "application/octet-stream")

(defn context-mimetype
  "Get the mimetype of a filename using the ServletContext."
  [context filename]
  (or (. context (getMimeType filename))
      *default-mimetype*))

;;;; Routes ;;;;

(def symbol-regex (re-pattern ":([a-z_]+)"))

(defn parse-route
  "Turn a route string into a regex and seq of symbols."
  [route]
  (let [segment  "([^/.,;?]+)"
        matcher  (re-matcher symbol-regex (re-escape route))
        symbols  (re-find-all matcher)
        regex    (. matcher (replaceAll segment))]
    [(re-pattern regex) (map second symbols)]))

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
  consisting of a HTTP method, a parsed route, function that takes in
  a context, request and response object.
  e.g.
  [\"GET\"
   (parse-route \"/welcome/:name\") 
   (fn [context request response] ...)]"}
  *resources* '())

(defn assoc-route
  "Associate a HTTP method and route with a resource."
  [method route resource]
  (def *resources*
    (cons [method (parse-route route) resource]
          *resources*)))

;;;; Response ;;;;

(defn update-response
  "Destructively update a HttpServletResponse via a Clojure datatype.
     * FixNum        - updates status code
     * map           - updates headers
     * string or seq - updates body
  Additionally, multiple updates can be chained through a vector.

  e.g (update-response resp ctx \"Foo\")       ; write 'Foo' to response body
      (update-response resp ctx [200 \"Bar\"]) ; set status 200, write 'Bar'"
  [#^HttpServletResponse response context update]
  (cond 
    (vector? update)
      (doseq d update
        (update-response response context d))
    (string? update)
      (.. response (getWriter) (print update))
    (seq? update)
      (let [writer (. response (getWriter))]
        (doseq d update
          (. writer (print d))))
    (map? update)
      (doseq [k v] update
        (. response (setHeader k v)))
    (instance? clojure.lang.FixNum update)
      (. response (setStatus update))
    (instance? java.io.File update)
      (let [out (. response (getOutputStream))
            in  (new FileInputStream update)]
        (. response (setHeader
          "Content-Type" (context-mimetype context (str update))))
        (pipe-stream in out))))

;;;; Resource ;;;;

(def #^{:doc
  "A set of bindings available to each resource. This can be extended
  by plugins, if required."}
  *resource-bindings*
  '(method    (. request (getMethod))
    full-path (. request (getPathInfo))
    param    #(. request (getParameter %))
    header   #(. request (getHeader %))
    mime     #(context-mimetype (str %))))

(defmacro new-resource
  "Create a pseudo-servlet from a resource. It's not quite a real
  servlet because it's a function, rather than an HttpServlet object."
  [& body]
  `(fn ~'[route context request response]
     (let ~(apply vector *resource-bindings*)
       (update-response ~'response ~'context (do ~@body)))))

(def *default-resource*
  (new-resource
    (let [static-file (file "public" full-path)]
      (if (. static-file (isFile))
        static-file
        [404 "Cannot find file"]))))

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
      (some matches? *resources*)
      (partial *default-resource* {}))))
