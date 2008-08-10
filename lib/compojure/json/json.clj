;; compojure.json -- JSON generator library for Compojure

(clojure/in-ns 'compojure.json)
(clojure/refer 'clojure)
(clojure/refer 'clojure.contrib.lib)

(use '(clojure.contrib str-utils))

(def json)

(defn json-list
  [coll]
  (str-join ", " (map json coll)))

(defn- json-array
  [coll]
  (str "[" (json-list coll) "]"))

(defn- json-object
  [hash]
  (let [pair (fn [[key val]]
               (str (json key) ": " (json val)))]
    (str "{" (str-join ", " (map pair hash)) "}")))

(defn json
  "Parse a Clojure datatype into a portable JSON format."
  [x]
  (cond
    (nil? x)             "null"
    (vector? x)          (json-array x)
    (map? x)             (json-object x)
    (string? x)          (pr-str x)
    (keyword? x)         (pr-str (name x))
    (instance? Number x) (pr-str x)))
