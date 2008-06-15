(def json)

(defn- json-array
  [coll]
  (str "[" (str-join (map json coll) ", ") "]"))

(defn- json-object
  [hash]
  (let [pair (fn [[k v]]
               (str (json k) ": " (json v)))]
    (str "{" (str-join (map pair hash) ", ") "}")))

(defn json
  [x]
  (cond
    (nil? x)             "null"
    (vector? x)          (json-array x)
    (map? x)             (json-object x)
    (string? x)          (pr-str x)
    (keyword? x)         (pr-str (name x))
    (instance? Number x) (pr-str x)))
