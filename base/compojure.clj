(in-ns 'compojure)
(clojure/refer 'clojure)
(import '(javax.servlet.http HttpServletResponse))

(defn includes?
  "Returns true if x is contained in coll, else false."
  [x coll]
  (some (partial = x) coll))

(defn re-escape
  "Escape all special regex chars in a string s."
  [#^String s]
  (let [chars  "\\.*+|?()[]{}$^"
        escape #(if (includes? % chars) [\\ %] [%])]
    (apply str (mapcat escape s))))

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
