(ns test.compojure.http.request
  (:use compojure.http.request)
  (:use clojure.contrib.test-is)
  (:use test.helpers))

(deftest query-params
  (are (= (parse-query-params {:query-string _1}) _2)
    "a=1"     {:a "1"}
    "a=1&b=2" {:a "1", :b "2"}))

(deftest query-params-plus
  (is (= (parse-query-params {:query-string "a=1+2"})
         {:a "1 2"})))

(deftest query-params-space
  (is (= (parse-query-params {:query-string "a=1%202"})
         {:a "1 2"})))

(deftest urlencoded-charset
  (is (urlencoded-form?
        {:content-type "application/x-www-form-urlencoded; charset=UTF8"})))

(deftest form-params
  (are (= (parse-form-params (form-request _1)) _2)
    "a=1"     {:a "1"}
    "a=1&b=2" {:a "1", :b "2"}))

(deftest assoc-params-empty
  (is (= (assoc-params {})
         {:form-params {}, :query-params {}, :params {}})))

(deftest assoc-params-merge
  (let [request {:form-params {:a "1"}, :query-params {:b "2"}}]
    (is (= (assoc-params request)
           (assoc request :params {:a "1", :b "2"})))))

(deftest assoc-params-twice
  (let [request (form-request "a=1")]
    (is (= (:form-params (-> request assoc-params assoc-params))
           {:a "1"}))))

(deftest request-cookies
  (is (= (parse-cookies {:headers {"cookie" "a=1;b=2"}})
         {:a "1", :b "2"})))
