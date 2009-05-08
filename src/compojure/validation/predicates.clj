(ns compojure.validation.predicates
    (:use compojure.str-utils))

(defn present?
  "True if x is not nil and not an empty string."
  [x]
  (not (blank? x)))

(defn max-size
  "Returns a function to check a maximum size of a collection."
  [n]
  #(<= (count %) n))
