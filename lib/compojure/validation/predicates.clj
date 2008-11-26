(in-ns 'compojure.validation)

(defn blank?
  "True if x is nil or an empty string."
  [x]
  (or (nil? x) (= x "")))

(defn present? [x]
  "True if x is not nil and not an empty string."
  (not (blank? x)))

(defn- length-checker [len #^java.lang.String str]
  "asserts that str's length is <= len"
  (if str
    (<= (.length str) len)
    false))

(defn max-length [y]
  "returns a function that asserts that a string's length is less than or equal to y"
  (partial length-checker y))
