(ns compojure.response
  "Methods for generating Ring response maps"
  (:import java.util.Map
           [java.io File InputStream]
           [clojure.lang IDeref IFn ISeq]))

(defprotocol Renderable
  (render [this request]
    "Render the object into a form suitable for the given request map."))

(extend-type nil
  Renderable
  (render [_ _] nil))

(extend-type String
  Renderable
  (render [this _]
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    this}))

(extend-type Map
  Renderable
  (render [this _]
    (merge {:status 200, :headers {}, :body ""} this)))

(extend-type IFn
  Renderable
  (render [this request]
    (render (this request) request)))

(extend-type IDeref
  Renderable
  (render [this request]
    (render (deref this) request)))

(extend-type File
  Renderable
  (render [this _]
    {:status 200, :headers {}, :body file}))

(extend-type ISeq
  Renderable
  (render [this _]
    {:status 200, :headers {}, :body coll}))

(extend-type InputStream
  (render [this _]
    {:status 200, :headers {}, :body stream}))
