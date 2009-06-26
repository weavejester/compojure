(ns test.compojure.crypto
  (:use compojure.crypto)
  (:use clojure.contrib.test-is))

(def secret-key
  (.getBytes "0123456789ABCDEF"))

(deftest test-seal-unseal
  (is (= (unseal secret-key (seal secret-key "Foobar"))
         "Foobar")))
