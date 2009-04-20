(ns test.compojure.http.helpers
  (:use compojure.http.helpers)
  (:use clojure.contrib.test-is))

(deftest test-set-cookie
  (is (= (set-cookie :foo "bar")
         {:headers {"Set-Cookie" "foo=bar"}})))

(deftest test-set-cookie-path
  (is (= (set-cookie :a "b", :path "/")
         {:headers {"Set-Cookie" "a=b; path=/"}})))

(deftest test-content-type
  (is (= (content-type "text/html")
         {:headers {"Content-Type" "text/html"}})))
