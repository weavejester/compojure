(ns compojure.http.helpers-test
  (:use compojure.http.helpers
        compojure.http.routes
        compojure.control
        clojure.contrib.test-is))

(deftest test-set-cookie
  (is (= (set-cookie :foo "bar")
         {:headers {"Set-Cookie" "foo=bar"}})))

(deftest test-set-cookie-path
  (is (= (set-cookie :a "b", :path "/")
         {:headers {"Set-Cookie" "a=b; path=/"}})))

(deftest test-content-type
  (is (= (content-type "text/html")
         {:headers {"Content-Type" "text/html"}})))

(deftest test-safe-path
  (is (not (safe-path? "/basedir/compojure" "../private/secret.txt")))
  (is (safe-path? "/basedir/compojure" "public/index.html")))
