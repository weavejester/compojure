(ns compojure.response
  "Methods for generating Ring response maps"
  (:import java.util.Map
           java.io.File
           [clojure.lang IDeref IFn]))

(defmulti render
  "Given the request map and an arbitrary value x, turn x into a valid HTTP
  response map. Dispatches on the type of x."
  (fn [_ x] (type x)))

(defmethod render nil [_ _] nil)

(defmethod render String [_ html]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    html})

(defmethod render Map [_ m]
  (merge {:status 200, :headers {}, :body ""} m))

(defmethod render IFn [request handler]
  (render request (handler request)))

(defmethod render IDeref [request ref-like]
  (render request (deref ref-like)))

(defmethod render File [_ file]
  {:status 200, :headers {}, :body file})

(prefer-method render Map IFn)
