(ns compojure.response-test
  (:use clojure.test
        compojure.response))

(deftest render-nil
  (is (nil? (render nil))))

(deftest render-html-string
  (is (= (render "<html><body>foo</body></html>")
         {:status 200
          :headers {"Content-Type" "text/html"}
          :body "<html><body>foo</body></html>"})))

(deftest render-map
  (is (= (render {:body "foo"})
         {:status 200
          :headers {}
          :body "foo"})))
