(def write-json)

(defn- json-array
  [coll]
  (str "[" (str-join (map write-json coll) ", ") "]"))

(defn- json-object
  [hash]
  (let [pair (fn [[k v]]
               (str (write-json k) ": " (write-json v)))]
    (str "{" (str-join (map pair hash) ", ") "}")))

(defn write-json
  [x]
  (cond
    (nil? x)             "null"
    (vector? x)          (json-array x)
    (map? x)             (json-object x)
    (string? x)          (pr-str x)
    (keyword? x)         (pr-str (name x))
    (instance? Number x) (pr-str x)))
