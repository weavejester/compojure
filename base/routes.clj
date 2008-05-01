(in-ns 'cljine)
(clojure/refer 'clojure)

(def symbol-regex  (re-pattern ":([a-z_]+)"))
(def segment-regex "([^/.,;?]+)")

(defn include?
  "Returns true if x is included in coll."
  [x coll]
    (some (partial = x) coll))

(defn re-escape
  "Escape all special regex chars in a string s."
  [#^String s]
    (let [chars  "\\.*+|?()[]{}$^"
          escape #(if (include? % chars) [\\ %] [%])]
      (apply str (mapcat escape s))))

(defn re-find-all
  "Repeat re-find for matcher m until nil, and return the seq of results."
  [m]
    (doall (take-while identity
      (map re-find (repeat m)))))

(defn parse-route
  "Turn a route string into a regex and seq of symbols."
  [route]
    (let [matcher  (re-matcher symbol-regex (re-escape route))
          symbols  (re-find-all matcher)
          regex    (. matcher (replaceAll segment-regex))]
      [(re-pattern regex) (map second symbols)]))

(defn match-route 
  "Match a path against a parsed route."
  [[regex symbols] path]
    (let [matcher (re-matcher regex path)]
      (if (. matcher (matches))
        (apply hash-map
          (interleave (map keyword symbols)
                      (rest (re-groups matcher)))))))
