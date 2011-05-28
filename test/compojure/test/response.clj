(ns compojure.test.response
  (:use clojure.test)
  (:require [clojure.java.io :as io])
  (:require [compojure.response :as response] :reload)
  (:import [java.io File InputStream]
           java.net.URL))

(def expected-response
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<h1>Foo</h1>"})

(deftest response-test
  (testing "with nil"
    (is (nil? (response/render nil {}))))

  (testing "with string"
    (is (= (response/render "<h1>Foo</h1>" {})
           expected-response)))

  (testing "with handler function"
    (is (= (response/render (constantly expected-response) {})
           expected-response)))

  (testing "with deref-able"
    (is (= (response/render (future expected-response) {})
           expected-response)))

  (testing "with file URL"
    (let [response (response/render (io/resource "resources/test.txt") {})]
      (is (instance? File (:body response)))
      (is (= (slurp (:body response)) "foobar\n"))))

  (testing "with stream URL"
    (let [response (response/render (io/resource "ring/util/response.clj") {})]
      (is (instance? InputStream (:body response)))
      (is (.contains (slurp (:body response)) "(ns ring.util.response")))))
