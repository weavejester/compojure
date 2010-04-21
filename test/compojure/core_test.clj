(ns compojure.core-test
  (:use clojure.test
        clojure.contrib.mock.test-adapter
        clojure.contrib.with-ns
        compojure.core
        compojure.response)
  (:require [compojure.test-namespace :as testns]))

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

(deftest route-with-method-param
  (let [req  {:request-method :post
              :form-params {"_method" "PUT"}
              :uri "/foo"}
        resp {:status 200, :headers {}, :body "bar"}
        route (PUT "/foo" [] resp)]
    (is (= (route req) resp))))

(defn func1 [x] (inc x))

(deftest wrap-var-with-funcion
  (let [wrapper (fn [f] (fn [x] (f (inc x))))]
    (wrap! func1 wrapper)
    (is (= (func1 3) 5))))

(defn func2 [x] (inc x))

(defn wrap-test1 [f]
  (fn [x] (f (* 2 x))))

(deftest wrap-var-with-keyword
  (wrap! func2 :test1)
  (is (= (func2 3) 7)))

(defn func3 [x] (inc x))

(deftest wrap-var-with-namespaced-keyword
  (wrap! func3 :testns/test2)
  (is (= (func3 3) 10)))

(defn func4 [x] (inc x))

(deftest wrap-var-with-function-and-keyword
  (wrap! func4 wrap-test1 :test1)
  (is (= (func4 3) 13)))
