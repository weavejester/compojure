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

  (testing "string parameter names"
    ((GET "/:x" [x y & more]
       (is (= x "foo"))
       (is (= y "bar"))
       (is (= more {"z" "baz"}))
       nil)
     (-> (request :get "/foo")
         (assoc :params {"y" "bar", "z" "baz"}))))
  
  (testing "vector ':as request' arguments"
    (let [req (-> (request :get "/foo")
                  (assoc :params {:y "bar"}))]
      ((GET "/:x" [x :as r]
            (is (= x "foo"))
            (is (= (dissoc r :params :route-params)
                   (dissoc req :params)))
            nil)
       req)))
  
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

(deftest routing-test
  (routing (request :get "/bar")
    (GET "/foo" [] (is false) nil)
    (GET "/bar" [] (is true) nil)))

(deftest routes-test
  ((routes
    (GET "/foo" [] (is false) nil)
    (GET "/bar" [] (is true) nil))
   (request :get "/bar")))

(deftest context-test
  (testing "matching"
    (let [handler (context "/foo/:id" [id] identity)]
      (is (map? (handler (request :get "/foo/10"))))
      (is (nil? (handler (request :get "/bar/10"))))))
  (testing "context key"
    (let [handler (context "/foo/:id" [id] :context)]
      (are [url ctx] (= (handler (request :get url)) ctx)
        "/foo/10"     "/foo/10"
        "/foo/10/bar" "/foo/10"
        "/bar/10"     nil)))
  (testing "path-info key"
    (let [handler (context "/foo/:id" [id] :path-info)]
      (are [url ctx] (= (handler (request :get url)) ctx)
        "/foo/10"     "/"
        "/foo/10/bar" "/bar"
        "/bar/10"     nil)))
  (testing "routes"
    (let [handler (context "/foo/:id" [id]
                    (GET "/" [] "root")
                    (GET "/id" [] id)
                    (GET "/x/:x" [x] x))]
      (are [url body] (= (:body (handler (request :get url)))
                         body)
        "/foo/10"    "root"
        "/foo/10/"   "root"   
        "/foo/10/id" "10"
        "/foo/1/x/2" "2"))))

(deftest wrap-test
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
