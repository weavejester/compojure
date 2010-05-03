(ns compojure.test.response
  (:use clojure.test)
  (:require [compojure.response :as response]))

(deftest response-with-nil
  (is (nil? (response/render {} nil))))

(deftest response-with-string
  (is (= (response/render {} "<h1>Foo</h1>")
         {:status  200
          :headers {"Content-Type" "text/html"}
          :body    "<h1>Foo</h1>"})))
