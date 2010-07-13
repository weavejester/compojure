(ns compojure.test.core
  (:use clojure.test
        clojure.contrib.mock.test-adapter
        clojure.contrib.with-ns
        compojure.core
        compojure.response
        clout.core)
  (:require [compojure.test-namespace :as testns]))

(deftest request-destructuring
  (testing "vector arguments"
    ((GET "/foo" [x y]
       (is (= x "bar"))
       (is (= y "baz"))
       nil)
    {:request-method :get
     :uri "/foo"
     :params {"x" "bar", "y" "baz"}}))
  
  (testing "map arguments"
    ((GET "/foo" {params :params}
       (is (= (params {:x "a", :y "b"})))
       nil)
    {:request-method :get
     :uri "/foo"
     :params {"x" "a", "y" "b"}})))

(deftest route-matching
  (testing "_method parameter"
    (let [req  {:request-method :post
                :form-params {"_method" "PUT"}
                :uri "/foo"}
          resp {:status 200, :headers {}, :body "bar"}
          route (PUT "/foo" [] resp)]
      (is (= (route req) resp))))

  (testing "custom regexes"
    (expect [route-compile
              (has-args ["/foo/:id" {:id "[0-9]+"}]
                (times 1))]
      (eval `(GET ["/foo/:id" :id "[0-9]+"] [])))))

(deftest wrap
  (testing "wrap function"
    (defn func1 [x] (inc x))
    (let [wrapper (fn [f] (fn [x] (f (inc x))))]
      (wrap! func1 wrapper)
      (is (= (func1 3) 5))))

  (testing "wrap keyword"
    (defn func2 [x] (inc x))
    (defn wrap-test1 [f]
      (fn [x] (f (* 2 x))))
    (wrap! func2 :test1)
    (is (= (func2 3) 7)))

  (testing "wrap namespaced keyword"
    (defn func3 [x] (inc x))
    (wrap! func3 :testns/test2)
    (is (= (func3 3) 10)))

  (testing "wrap function and keyword"
    (defn func4 [x] (inc x))
    (wrap! func4 wrap-test1 :test1)
    (is (= (func4 3) 13))))
