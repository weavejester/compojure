(in-ns 'compojure.validation)
(use 'clojure.contrib.test-is)

(deftest test-blank
  (is (= false (blank? 42)))
  (is (= true (blank? nil)))
  (is (= true (blank? "")))
  (is (= false (blank? "hello"))))

(deftest length-checker-under
  (is (= (length-checker 10 "ten chars ") true)))

(deftest length-checker-over
  (is (= (length-checker 7 "too long") false)))

(deftest length-checker-handles-null-str
  (is (= (length-checker 10 nil) false)))