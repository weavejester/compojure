(in-ns 'compojure.validation)

(defn blank? [x]
  (or (nil? x) (= x "")))

(defn present? [x] 
  (not (blank? x)))

(defn- length-checker [len #^java.lang.String str]
  "asserts that str's length is <= len"
  (if str
    (<= (.length str) len)
    false))

(defn max-length [y]
  "returns a function that asserts that a string's length is less than or equal to y"
  (partial length-checker y))