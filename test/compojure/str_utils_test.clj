(ns compojure.str-utils-test
  (:use compojure.str-utils
        clojure.contrib.test-is))

(deftest test-escape
  (is (= (escape "aeiou" "hello world")
         "h\\ell\\o w\\orld")))
