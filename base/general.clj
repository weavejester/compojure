(in-ns 'clojure)

(defn include?
  "Returns true if x is included in coll."
  [x coll]
    (some (partial = x) coll))
