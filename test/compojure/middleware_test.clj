(ns compojure.middleware-test
  (:require [clojure.test :refer :all]
            [compojure.core :refer [routes GET]]
            [compojure.middleware :refer [wrap-canonical-redirect]]
            [compojure.route :as route]
            [ring.mock.request :as mock]
            [ring.util.response :as resp]))

(deftest canonical-redirect-test
  (let [handler (wrap-canonical-redirect
                 (routes
                  (GET "/foo" [] "foo")
                  (GET "/bar" [] "bar")
                  (route/not-found "not found")))]
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
           {:status 404
            :headers {"Content-Type" "text/html; charset=utf-8"}
            :body "not found"}))))

(deftest canonical-redirect-async-test
  (let [handler (wrap-canonical-redirect (GET "/foo" [] "foo"))]
    (testing "matching route"
      (let [response  (promise)
            exception (promise)]
        (handler (mock/request :get "/foo") response exception)
        (is (not (realized? exception)))
        (is (= @response
               {:status 200
                :headers {"Content-Type" "text/html; charset=utf-8"}
                :body "foo"}))))

    (testing "redirect"
      (let [response  (promise)
            exception (promise)]
        (handler (mock/request :get "/foo/") response exception)
        (is (not (realized? exception)))
        (is (= @response
               {:status 301
                :headers {"Location" "/foo"}
                :body ""}))))))
