(ns compojure.validation)

(defn blank?
  "True if x is nil or an empty string."
  [x]
  (or (nil? x) (= x "")))

(defn present?
  "True if x is not nil and not an empty string."
  [x]
  (not (blank? x)))

(defn max-size
  "Returns a function to check a maximum size of a collection."
  [n]
  #(<= (count %) n))
