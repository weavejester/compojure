(ns compojure.response
  "Methods for generating Ring response maps"
  (:use [clojure.core.incubator :only (-?>)]
        [ring.util.response :only (response content-type)])
  (:require [clojure.java.io :as io])
  (:import [java.io File InputStream]
           [java.net URL]
           [clojure.lang APersistentMap IDeref IFn ISeq]))

(defprotocol Renderable
  (render [this request]
    "Render the object into a form suitable for the given request map."))

(extend-protocol Renderable
  nil
  (render [_ _] nil)
  String
  (render [body _]
    (-> (response body)
        (content-type "text/html; charset=utf-8")))
  APersistentMap
  (render [resp-map _]
    (merge (with-meta (response "") (meta resp-map))
           resp-map))
  IFn
  (render [func request]
    (render (func request) request))
  IDeref
  (render [ref request]
    (render (deref ref) request))
  File
  (render [file _] (response file))
  ISeq
  (render [coll _] (-> (response coll)
                       (content-type "text/html; charset=utf-8")))
  InputStream
  (render [stream _] (response stream))
  URL
  (render [url _]
    (if (= "file" (.getProtocol url))
      (response (io/as-file url))
      (response (io/input-stream url)))))
