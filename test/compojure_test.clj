(ns compojure-test
  (:use clojure.test)
  (:use compojure))

(deftest basic-get-test
  (let [handler (GET "/:x" [x] x)
        request {:request-method :get, :uri "/foo"}]
    (is (= (handler request) "foo"))))
