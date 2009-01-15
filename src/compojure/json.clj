;; compojure.json
;;
;; Small library for formatting standard Clojure datatype as a JSON string.

(ns compojure.json
  (:use (clojure.contrib str-utils)))

(def json)

(defn- json-array
  [coll]
  (str "[" (str-join ", " (map json coll)) "]"))

(defn- json-object
  [hash]
  (let [pair (fn [[key val]]
               (str (json key) ": " (json val)))]
    (str "{" (str-join ", " (map pair hash)) "}")))

(defn json
  "Format a Clojure datatype into a portable JSON format."
  [x]
  (cond
    (nil? x)     "null"
    (seq? x)     (json-array x)
    (vector? x)  (json-array x)
    (map? x)     (json-object x)
    (string? x)  (pr-str x)
    (keyword? x) (pr-str (name x))
    (number? x)  (pr-str x)
    :else        (str x)))
