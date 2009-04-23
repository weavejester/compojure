(ns test.helpers
  (:import java.io.ByteArrayInputStream)
  (:import java.io.File))

(defn input-stream [s]
  (ByteArrayInputStream. (.getBytes s)))

(defn form-request [body]
  {:content-type "application/x-www-form-urlencoded"
   :body (input-stream body)})

(defn temp-file []
  (doto (File/createTempFile "compojure" "test")
    (.deleteOnExit)))
