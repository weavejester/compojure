(ns compojure.middleware-test
  (:require [clojure.test :refer :all]
            [compojure.core :refer [routes GET]]
            [compojure.middleware :refer [wrap-canonical-redirect]]
            [ring.mock.request :as mock]))

(deftest test-canonical-redirect
  (let [handler (wrap-canonical-redirect
                 (routes
                  (GET "/foo" [] "foo")
                  (GET "/bar" [] "bar")))]
    (is (= (handler (mock/request :get "/foo"))
           {:status 200
            :headers {"Content-Type" "text/html; charset=utf-8"}
            :body "foo"}))
    (is (= (handler (mock/request :get "/foo/"))
           {:status 301
            :headers {"Location" "/foo"}
            :body ""}))
    (is (= (handler (mock/request :get "/bar/"))
           {:status 301
            :headers {"Location" "/bar"}
            :body ""}))
    (is (= (handler (mock/request :get "/baz/"))
           nil))))
