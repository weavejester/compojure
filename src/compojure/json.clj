;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.json:
;; 
;; A library for outputting basic Clojure types in JSON.

(ns compojure.json
  (:use clojure.contrib.str-utils))

(declare json)

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
