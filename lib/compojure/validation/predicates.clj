(defn blank?
  ([x] (or (nil? x) (= x ""))))

(deftest test-blank
  (is (= false (blank? 42)))
  (is (= true (blank? nil)))
  (is (= true (blank? "")))
  (is (= false (blank? "hello"))))

(defn present?
  ([x] (not (blank? x))))
