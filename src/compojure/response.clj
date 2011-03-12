(ns compojure.response
  "Methods for generating Ring response maps"
  (:use clojure.contrib.core
        [ring.util.response :only (response content-type)])
  (:require [clojure.java.io :as io])
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

(defn- servlet-resource-stream [path request]
  (-?> (:servlet-context request)
       (.getResourceAsStream (str "/" path))))

(defn- classpath-resource-stream [path]
  (-?> (io/resource path)
       (io/input-stream)))

(defn- resource-stream [path request]
  (or (classpath-resource-stream path)
      (servlet-resource-stream path request)))

(deftype Resource [path]
  Renderable
  (render [_ request]
    (-?> (resource-stream path request)
         (response))))

(defn resource
  "Create a resource response."
  [^String path]
  (Resource. path))
