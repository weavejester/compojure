(ns compojure.response
  "Methods for generating Ring response maps"
  (:import java.util.Map))

(defmulti render
  "Turns its argument into an appropriate response"
  type)

(defmethod render nil [_] nil)

(defmethod render String [html-string]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body html-string})

(defmethod render Map [a-map]
  (merge {:status 200, :headers {}, :body ""}
         a-map))
