(ns compojure.core-test
  (:use clojure.test
        clojure.contrib.mock.test-adapter
        compojure.core
        compojure.response))

(deftest route-parameter-arguments
  ((GET "/foo" [x y]
     (is (= x "bar"))
     (is (= y "baz"))
     nil)
   {:request-method :get
    :uri "/foo"
    :params {:x "bar", :y "baz"}}))
