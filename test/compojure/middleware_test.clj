(ns compojure.middleware-test
  (:require [clojure.test :refer :all]
            [compojure.core :refer :all]
            [compojure.middleware :refer :all]
            [ring.mock.request :as mock]))

(deftest wrap-canonical-redirect-test
  (testing "with :compojure.middleware/slash-removed? flag"
    (let [route (-> (GET "/foo" req "response")
                    (wrap-canonical-redirect remove-trailing-slash))]
      (testing "request to /foo/ should return 301 redirect"
        (let [response (route (mock/request :get "/foo/"))]
          (is (empty? (:body response)))
          (is (= 301 (:status response)))
          (is (= "/foo" (-> response :headers (get "Location"))))))
      (testing "request to /foo should return 'response'"
        (let [response (route (mock/request :get "/foo"))]
          (is (= "response" (:body response)))
          (is (= 200 (:status response))))))))
