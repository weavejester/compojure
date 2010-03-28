(ns compojure.core-test
  (:use clojure.test
        clojure.contrib.mock.test-adapter
        compojure.core
        compojure.response))

(deftest route-with-vector-arguments
  ((GET "/foo" [x y]
     (is (= x "bar"))
     (is (= y "baz"))
     nil)
   {:request-method :get
    :uri "/foo"
    :params {"x" "bar", "y" "baz"}}))

(deftest route-with-map-arguments
  ((GET "/foo" {params :params}
     (is (= (params {:x "a", :y "b"})))
     nil)
   {:request-method :get
    :uri "/foo"
    :params {"x" "a", "y" "b"}}))
