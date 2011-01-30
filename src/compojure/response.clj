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

(defn- servlet-resource-stream [context path]
  (.getResourceAsStream context (str "/" path)))

(defn- thread-resource-stream [path]
  (.. Thread
      currentThread
      getContextClassLoader
      (getResourceAsStream path)))

(defn- resource-stream [path request]
  (if-let [context (:servlet-context request)]
    (servlet-resource-stream context path)
    (thread-resource-stream path)))

(deftype Resource [path]
  Renderable
  (render [_ request]
    (if-let [stream (resource-stream path request)]
      (response stream))))

(defn resource
  "Create a resource response."
  [^String path]
  (Resource. path))
