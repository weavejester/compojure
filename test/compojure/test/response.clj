(ns compojure.test.response
  (:use clojure.test)
  (:require [clojure.java.io :as io])
  (:require [compojure.response :as response] :reload)
  (:import [java.io File InputStream]
           java.net.URL))

(def expected-response
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    "<h1>Foo</h1>"})

(deftest response-test
  (testing "with nil"
    (is (nil? (response/render nil {}))))

  (testing "with string"
    (is (= (response/render "<h1>Foo</h1>" {})
           expected-response)))

  (testing "with string seq"
    (let [response (response/render '("<h1>" "Foo" "</h1>") {})]
      (is (seq? (:body response)))
      (is (= (:headers response)
             {"Content-Type" "text/html; charset=utf-8"}))))

  (testing "with handler function"
    (is (= (response/render (constantly expected-response) {})
           expected-response)))

  (testing "with deref-able"
    (is (= (response/render (future expected-response) {})
           expected-response)))

  (testing "with file URL"
    (let [response (response/render (io/resource "resources/test.txt") {})]
      (is (instance? File (:body response)))
      (is (= (get-in response [:headers "Content-Length"]) "7"))
      (is (string? (get-in response [:headers "Last-Modified"])))
      (is (= (slurp (:body response)) "foobar\n"))))

  (testing "with stream URL"
    (let [response (response/render (io/resource "ring/util/response.clj") {})
          body-str (slurp (:body response))]
      (is (instance? InputStream (:body response)))
      (is (= (get-in response [:headers "Content-Length"]) (str (count body-str))))
      (is (string? (get-in response [:headers "Last-Modified"])))
      (is (.contains body-str "(ns ring.util.response"))))
  
  (testing "with map + metadata"
    (let [response (response/render ^{:has-metadata? true} {:body "foo"} {})]
      (is (= (:body response) "foo"))
      (is (= (meta response) {:has-metadata? true})))))
