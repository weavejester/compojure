(ns compojure.response
  "Methods for generating Ring response maps"
  (:use [ring.util.response :only (response content-type)])
  (:import [java.io File InputStream]
           [clojure.lang APersistentMap IDeref IFn ISeq]))

(defprotocol Renderable
  (render [this request]
    "Render the object into a form suitable for the given request map."))

(extend-protocol Renderable
  nil
  (render [_ _] nil)
  String
  (render [this _]
    (-> (response this)
        (content-type "text/html")))
  APersistentMap
  (render [this _]
    (merge (response "") this))
  IFn
  (render [this request]
    (render (this request) request))
  IDeref
  (render [this request]
    (render (deref this) request))
  File
  (render [this _] (response this))
  ISeq
  (render [this _] (response this))
  InputStream
  (render [this _] (response this)))
