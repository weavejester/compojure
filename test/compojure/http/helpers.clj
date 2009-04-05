(ns test.compojure.http.helpers
  (:use compojure.http.helpers)
  (:use clojure.contrib.test-is))

(deftest test-set-cookie
  (let [response (set-cookie :foo "bar")]
    (= (get-in response [:headers "Set-Cookie"])
       "foo=bar")))
