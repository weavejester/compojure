(ns test.compojure.http.request
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.request)
  (:import java.io.ByteArrayInputStream))

(def single-param-pair #"(\w+)=(\w+)")

(defn- input-stream
  "Create an input stream from a string"
  [s]
  (ByteArrayInputStream. (.getBytes s)))

(fact "Parameters can be passed via the query string"
  [[params name value] single-param-pair]
  (= (get-query-params {:query-string params})
     {(keyword name) value}))

(fact "Parameters can be passed via the body"
  [[params name value] single-param-pair]
  (let [request {:content-type "application/x-www-form-urlencoded"
                 :body         (input-stream params)}]
    (= (get-form-params request)
       {(keyword name) value})))

(fact "Cookies can be passed via the 'cookie' HTTP header"
  [[cookie name value] single-param-pair]
  (= (get-cookies {:headers {"cookie" cookie}})
     {(keyword name) value}))

(fact "get-query-params returns empty map if no parameters found"
  []
  (= (get-query-params {}) {}))

(fact "get-form-params returns empty map if no parameters found"
  [request [{} {:body ""}]]
  (= (get-query-params {}) {}))

(fact "get-query-params preserves parameters from request"
  [[_ key1 val1]     single-param-pair
   [query key2 val2] single-param-pair]
  (let [params1 {(keyword key1) val1}
        params2 {(keyword key2) val2}
        request {:query-params params1, :query-string query}]
    (= (get-query-params request)
       (merge params1 params2))))

(fact "get-form-params preserves parameters from request"
  [[_ key1 val1]    single-param-pair
   [body key2 val2] single-param-pair]
  (let [params1 {(keyword key1) val1}
        params2 {(keyword key2) val2}
        request {:form-params  params1
                 :content-type "application/x-www-form-urlencoded"
                 :body         (input-stream body)}]
    (= (get-form-params request)
       (merge params1 params2))))
