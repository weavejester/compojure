(ns compojure.test.route
  (:use clojure.test
        [clojure.contrib.io :only (slurp*)])
  (:require [compojure.route :as route]))

(deftest not-found-route
  (let [response ((route/not-found "foo") {})]
    (is (= (:status response) 404))
    (is (= (:body response) "foo"))))

(deftest resources-route
  (let [route    (route/resources "/foo" {:root "/resources"})
        response (route {:request-method :get, :uri "/foo/test.txt"})]
    (is (= (:status response) 200))
    (is (= (slurp* (:body response)) "foobar\n"))))
