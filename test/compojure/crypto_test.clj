(ns compojure.crypto-test
  (:use compojure.crypto
        clojure.contrib.test-is))

(deftest secret-key-length
  (are (= (count (gen-secret-key {:key-size _1})) _2)
    256 32
    128 16))

(deftest secret-key-uniqueness
  (let [a (gen-secret-key {:key-size 128})
        b (gen-secret-key {:key-size 128})]
    (is (not= a b))))

(def secret-key
  (.getBytes "0123456789ABCDEF"))

(deftest seal-string
  (is (not= (seal secret-key "Foobar") "Foobar")))

(deftest seal-uniqueness
  (let [a (seal secret-key "Foobar")
        b (seal secret-key "Foobar")]
    (is (not= a b))))

(deftest seal-then-unseal
  (are (= (unseal secret-key (seal secret-key _1)) _1)
    "Foobar"
    [1 2 3]
    {:a 10}))

(deftest seal-then-tamper
  (let [data (seal secret-key "Foobar")
        data (apply str "A" (rest data))]
    (is (nil? (unseal secret-key "Foobar")))))
