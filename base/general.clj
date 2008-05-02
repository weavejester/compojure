(in-ns 'clojure)

(defn include?
  "Returns true if x is contained in coll, else false."
  [x coll]
    (some (partial = x) coll))

(defn number?
  "Returns true if an Clojure number type."
  [x]
  (or (instance? clojure.lang.FixNum x)
      (instance? clojure.lang.DoubleNum x)))
