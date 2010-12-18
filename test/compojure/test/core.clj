(ns compojure.test.core
  (:use clojure.test
        ring.mock.request
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
     (-> (request :get "/foo")
         (assoc :params {:x "bar", :y "baz"}))))

  (testing "vector '& more' arguments"
    ((GET "/:x" [x y & more]
       (is (= x "foo"))
       (is (= y "bar"))
       (is (= more {:z "baz"}))
       nil)
     (-> (request :get "/foo")
         (assoc :params {:y "bar", :z "baz"}))))
  
  (testing "map arguments"
    ((GET "/foo" {params :params}
       (is (= (params {:x "a", :y "b"})))
       nil)
     (-> (request :get "/foo")
         (assoc :params {:x "a", :y "b"})))))

(deftest route-matching
  (testing "_method parameter"
    (let [req (-> (request :post "/foo")
                  (assoc :form-params {"_method" "PUT"}))
          resp {:status 200, :headers {}, :body "bar"}
          route (PUT "/foo" [] resp)]
      (is (= (route req) resp))))

  (testing "custom regular expressions"
    (expect [route-compile
              (has-args ["/foo/:id" {:id "[0-9]+"}]
                (times 1))]
      (eval `(GET ["/foo/:id" :id "[0-9]+"] [])))))

(deftest routes-test
  (testing "keyword parameters"
    ((routes
      (GET "/:x" [x y & more]
        (is (= x "foo"))
        (is (= y "bar"))
        (is (= more {:z "baz"}))
        nil))
     (request :get "/foo" {:y "bar", :z "baz"})))
  (testing "nested parameters"
    ((routes
      (GET "/" [x y]
        (is (= x {:a "1", :b "2"}))
        (is (= y ["3" "4"]))
        nil))
     (request :get "/" [["x[a]" "1"]
                        ["x[b]" "2"]
                        ["y[]" "3"]
                        ["y[]" "4"]]))))

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
