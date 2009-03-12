(ns test.compojure.http.request
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.request)
  (:import java.io.ByteArrayInputStream))

(def single-param-pair #"(\w+)=(\w+)")

(fact "Parameters can be passed via the query string"
  [[params name value] single-param-pair]
  (= (get-query-params {:query-string params})
     {(keyword name) value}))

(fact "Parameters can be passed via the body"
  [[params name value] single-param-pair]
  (let [request {:content-type "application/x-www-form-urlencoded"
                 :body         (ByteArrayInputStream. (.getBytes params))}]
    (= (get-form-params request)
       {(keyword name) value})))

(fact "Cookies can be passed via the 'cookie' HTTP header"
  [[cookie name value] single-param-pair]
  (= (get-cookies {:headers {"cookie" cookie}})
     {(keyword name) value}))
