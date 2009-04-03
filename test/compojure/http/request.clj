(ns test.compojure.http.request
  (:use fact.core)
  (:use fact.random-utils)
  (:use re-rand)
  (:use clojure.contrib.str-utils)
  (:use compojure.http.request)
  (:import java.io.ByteArrayInputStream))

(defn- random-params []
  (random-map random-keyword #(re-rand #"\w+") 1 10))

(defn- naively-encode
  "Encode a map of parameters without urlencoding them first."
  [params sep]
  (str-join sep (for [[k v] params] (str (name k) "=" v))))

(defn- input-stream
  "Create an input stream from a string"
  [s]
  (ByteArrayInputStream. (.getBytes s)))

(fact "Parameters can be passed via the query string"
  [params random-params]
  (let [query   (naively-encode params "&")
        request {:query-string query}]
    (= (get-query-params request)
       params)))

(fact "The form-urlencoded content-type can have an charset"
  [charset #"\w+"]
  (let [type (str "application/x-www-form-urlencoded; charset=" charset)]
    (urlencoded-form? {:content-type type})))

(fact "Parameters can be passed via the body"
  [params random-params]
  (let [body    (input-stream (naively-encode params "&"))
        type    "application/x-www-form-urlencoded"
        request {:content-type type, :body body}]
    (= (get-form-params request)
       params)))

(fact "Cookies can be passed via the 'cookie' HTTP header"
  [cookies random-params]
  (let [headers {"cookie" (naively-encode cookies "; ")}
        request {:headers headers}]
    (= (get-cookies request)
       cookies)))

(fact "get-query-params returns empty map if no parameters found"
  []
  (= (get-query-params {}) {}))

(fact "get-form-params returns empty map if no parameters found"
  [request [{} {:body ""}]]
  (= (get-query-params {}) {}))

(fact "get-query-params preserves parameters from request"
  [params1 random-params
   params2 random-params]
  (let [query   (naively-encode params2 "&")
        request {:query-params params1, :query-string query}]
    (= (get-query-params request)
       (merge params1 params2))))

(fact "get-form-params preserves parameters from request"
  [params1 random-params
   params2 random-params]
  (let [body    (naively-encode params2 "&")
        request {:form-params  params1
                 :content-type "application/x-www-form-urlencoded"
                 :body         (input-stream body)}]
    (= (get-form-params request)
       (merge params1 params2))))
