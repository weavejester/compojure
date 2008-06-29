(in-ns* 'javascript)
(def json)

(defn json-list
  [coll]
  (str-join (map json coll) ", "))

(defn- json-array
  [coll]
  (str "[" (json-list coll) "]"))

(defn- json-object
  [hash]
  (let [pair (fn [[k v]]
               (str (json k) ": " (json v)))]
    (str "{" (str-join (map pair hash) ", ") "}")))

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

(defmacro js
  "Generates javascript code from an sexpr. Currently only handles functions.
  e.g. (js alert \"Hello World\") => \"alert(\"Hello World\");\""
  [name & args]
  `(str '~name "(" (json-list (list ~@args)) ");"))
