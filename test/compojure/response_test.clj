(ns compojure.response-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [compojure.response :as response]))

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

  (testing "with file"
    (let [response (response/render (io/file "test/test_files/test.txt") {})]
      (is (instance? java.io.File (:body response)))
      (is (= (get-in response [:headers "Content-Length"]) "7"))
      (is (= (get-in response [:headers "Content-Type"]) "text/plain"))
      (is (string? (get-in response [:headers "Last-Modified"])))
      (is (= (slurp (:body response)) "foobar\n"))))

  (testing "with file URL"
    (let [response (response/render (io/resource "test_files/test.txt") {})]
      (is (instance? java.io.File (:body response)))
      (is (= (get-in response [:headers "Content-Length"]) "7"))
      (is (= (get-in response [:headers "Content-Type"]) "text/plain"))
      (is (string? (get-in response [:headers "Last-Modified"])))
      (is (= (slurp (:body response)) "foobar\n"))))

  (testing "with stream URL"
    (let [response (response/render (io/resource "ring/util/response.clj") {})
          body-str (slurp (:body response))]
      (is (instance? java.io.InputStream (:body response)))
      (is (= (get-in response [:headers "Content-Length"]) (str (count body-str))))
      (is (nil? (get-in response [:headers "Content-Type"])))
      (is (string? (get-in response [:headers "Last-Modified"])))
      (is (.contains body-str "(ns ring.util.response"))))

  (testing "with map + metadata"
    (let [response (response/render ^{:has-metadata? true} {:body "foo"} {})]
      (is (= (:body response) "foo"))
      (is (= (meta response) {:has-metadata? true}))))

  (testing "with vector"
    (is (thrown-with-msg? IllegalArgumentException
                          #"No implementation of method: :render of protocol: #'compojure.response/Renderable"
                          (response/render [] {})))))
