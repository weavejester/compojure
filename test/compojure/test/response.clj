(ns compojure.test.response
  (:use [clojure.test] :reload)
  (:require [compojure.response :as response]))

(deftest response-with-nil
  (is (nil? (response/render nil {}))))

(def test-response
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<h1>Foo</h1>"})

(deftest response-with-string
  (is (= (response/render "<h1>Foo</h1>" {})
         test-response)))

(deftest response-with-fn
  (is (= (response/render (constantly test-response) {})
         test-response)))

(deftest response-with-deref
  (is (= (response/render (future test-response) {})
         test-response)))

(deftest response-resource
  (let [resource (response/resource "resources/test.txt")
        response (response/render resource {})]
    (is (= (slurp (:body response)) "foobar\n"))))
