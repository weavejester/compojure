(ns compojure.test.route
  (:use clojure.test
        ring.mock.request)
  (:require [compojure.route :as route] :reload))

(deftest not-found-route
  (testing "string body"
    (let [response ((route/not-found "foo") (request :get "/"))]
      (is (= (:status response) 404))
      (is (= (:body response) "foo"))))
  (testing "response map body"
    (let [response ((route/not-found {:status 200 :body "bar"})
                    (request :get "/"))]
      (is (= (:status response) 404))
      (is (= (:body response) "bar")))))

(deftest resources-route
  (let [route    (route/resources "/foo" {:root "resources"})
        response (route (request :get "/foo/test.txt"))]
    (is (= (:status response) 200))
    (is (= (slurp (:body response)) "foobar\n"))
    (is (= (get-in response [:headers "Content-Type"])
           "text/plain"))))

(deftest files-route
  (let [route    (route/files "/foo" {:root "test/resources"})
        response (route (request :get "/foo/test.txt"))]
    (is (= (:status response) 200))
    (is (= (slurp (:body response)) "foobar\n"))
    (is (= (get-in response [:headers "Content-Type"])
           "text/plain"))))
