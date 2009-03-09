(ns test.compojure.http.request
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.request)
  (:import java.io.ByteArrayInputStream))

(fact "Parameters can be passed via the query string"
  [[params name value] #"(\w+)=(\w+)"]
  (= (get-query-params {:query-string params})
     {(keyword name) value}))

(fact "Parameters can be passed via the body"
  [[params name value] #"(\w+)=(\w+)"]
  (let [request {:content-type "application/x-www-form-urlencoded"
                 :body         (ByteArrayInputStream. (.getBytes params))}]
    (= (get-form-params request)
       {(keyword name) value})))
