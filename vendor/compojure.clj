(in-ns 'compojure)
(clojure/refer 'clojure)
(import '(java.io File))
(import '(javax.servlet.http HttpServletRequest HttpServletResponse))

(defn includes?
  "Returns true if x is contained in coll, else false."
  [x coll]
  (some (partial = x) coll))

(defn escape
  "Escape a set of special characters chars in a string s."
  [chars s]
  (apply str
    (mapcat #(if (includes? % chars) [\\ %] [%]) s)))

(defn grep
  "Filters a seq by a regular expression."
  [re coll]
  (filter #(re-matches re %) coll))

(defn re-escape
  "Escape all special regex chars in a string s."
  [s]
  (escape "\\.*+|?()[]{}$^" s))

(def symbol-regex  (re-pattern ":([a-z_]+)"))

(defn re-find-all
  "Repeat re-find for matcher m until nil, and return the seq of results."
  [m]
  (doall (take-while identity
    (map re-find (repeat m)))))

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
        (interleave (map keyword symbols)
                    (rest (re-groups matcher)))))))

(defn update-response!
  "Destructively update a HttpServletResponse via a Clojure datatype.
     * FixNum        - updates status code
     * map           - updates headers
     * string or seq - updates body
  Additionally, multiple updates can be chained through a vector.

  e.g (update-response! \"Foo\")       ; write 'Foo' to response body
      (update-response! [200 \"Bar\"]) ; set status to 200, and write 'Bar'"
  [#^HttpServletResponse response change]
  (cond 
    (string? change)
      (.. response (getWriter) (print change))
    (seq? change)
      (let [writer (. response (getWriter))]
        (doseq c change
          (. writer (print c))))
    (instance? clojure.lang.FixNum change)
      (. response (setStatus change))
    (map? change)
      (doseq [k v] change
        (. response (setHeader k v)))
    (vector? change)
      (doseq c change
        (update-response! response c))))

(def #^{:doc
  "A global list of all registered resources. A resource is a vector
  consisting of a HTTP method, a parsed route, and a list of evaluatable
  actions.
  e.g.
  [\"GET\" (parse-route \"/welcome/:name\")
   '((str \"Hello \" (path :name))]"}
  *resources* '())

(defn add-resource
  "Add a resource to the global *resources*."
  [method route body]
  (def *resources*
    (cons [method (parse-route route) body] *resources*)))

(defn find-resource
  "Find the first resource that matches the HttpServletRequest"
  [#^HttpServletRequest request]
  (let [method    (. request (getMethod))
        path      (. request (getPathInfo))
        matches?  (fn [[meth route resource]]
                    (if (= meth method)
                      (if-let route-params (match-route route path)
                         [route-params resource] nil)))]
    (some matches? *resources*)))

(defn resource-servlet
  "Create a pseudo-servlet from a resource. It's not quite a real
  servlet because it's just a function that takes in a request and
  a response object as arguments."
  [[route-params resource]]
  (eval
   `(fn ~'[request response]
      (let
        ~(apply vector
             'route   route-params
            '[method  (. request (getMethod))
              path    (. request (getPathInfo))
              param  #(. request (getParameter %))
              header #(. request (getHeader %))])
        (update-response! ~'response (do ~@resource))))))

; Add macros for GET, POST, PUT and DELETE
(doseq method '(GET POST PUT DELETE)
  (eval
   `(defmacro ~method
      ~'[route & body]
      (add-resource ~(str method) ~'route ~'body))))

(def sep (. File separator))

(defn load-file-pattern
  "Load all files matching a regular expression."
  [re]
  (doseq
    file (grep re (map str (file-seq (new File "."))))
    (load-file file)))
