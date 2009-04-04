(ns test.compojure.http.request
  (:use compojure.http.request)
  (:use clojure.contrib.test-is)
  (:import java.io.ByteArrayInputStream))

(deftest query-params
  (are (= (get-query-params {:query-string _1}) _2)
    "a=1"     {:a "1"}
    "a=1&b=2" {:a "1", :b "2"}))

(deftest query-params-plus
  (is (= (get-query-params {:query-string "a=1+2"})
         {:a "1 2"})))

(deftest query-params-space
  (is (= (get-query-params {:query-string "a=1%202"})
         {:a "1 2"})))

(deftest query-params-empty
  (is (= (get-query-params {}) {})))

(deftest query-params-merge
  (let [request {:query-params {:a "1"}, :query-string "b=2"}]
    (is (= (get-query-params request)
           {:a "1", :b "2"}))))

(deftest urlencoded-charset
  (is (urlencoded-form?
        {:content-type "application/x-www-form-urlencoded; charset=UTF8"})))

(defn- input-stream [s]
  (ByteArrayInputStream. (.getBytes s)))

(defn- form-request [body]
  {:content-type "application/x-www-form-urlencoded"
   :body (input-stream body)})

(deftest form-params
  (are (= (get-form-params (form-request _1)) _2)
    "a=1"     {:a "1"}
    "a=1&b=2" {:a "1", :b "2"}))

(deftest form-params-empty
  (is (= (get-form-params {}) {})))

(deftest form-params-merge
  (let [request (merge {:form-params {:a "1"}} (form-request "b=2"))]
    (is (= (get-form-params request)
           {:a "1", :b "2"}))))

(deftest request-cookies
  (is (= (get-cookies {:headers {"cookie" "a=1;b=2"}})
         {:a "1", :b "2"})))
