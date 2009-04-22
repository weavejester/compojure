(ns test.helpers
  (:import java.io.ByteArrayInputStream))

(defn input-stream [s]
  (ByteArrayInputStream. (.getBytes s)))

(defn form-request [body]
  {:content-type "application/x-www-form-urlencoded"
   :body (input-stream body)})
