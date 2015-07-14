(ns compojure.coercions-test
  (:require [clojure.test :refer :all]
            [compojure.coercions :refer :all]))

(deftest test-as-int
  (is (= (as-int "1") 1))
  (is (= (as-int "-1") -1))
  (is (= (as-int "0") 0))
  (is (nil? (as-int "1e2")))
  (is (nil? (as-int "1,000")))
  (is (nil? (as-int "")))
  (is (nil? (as-int "1.3")))
  (is (nil? (as-int "x")))
  (is (nil? (as-int "1f"))))

(deftest test-as-uuid
  (is (= (as-uuid "de305d54-75b4-431b-adb2-eb6b9e546014")
         #uuid "de305d54-75b4-431b-adb2-eb6b9e546014"))
  (is (nil? (as-uuid "")))
  (is (nil? (as-uuid "de305d5475b4431badb2eb6b9e54601z")))
  (is (nil? (as-uuid "de305d54-75b4-431b-adb2-eb6b9e5460")))
  (is (nil? (as-uuid "de305d54-75b4-431b-adb2-eb6b9e54601z"))))
