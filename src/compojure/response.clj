(ns compojure.response
  "A protocol for generating Ring response maps"
  (:require [ring.util.mime-type :as mime]
            [ring.util.response :as response]))

(defprotocol Renderable
  "A protocol that tells Compojure how to handle the return value of routes
  defined by GET, POST, etc.

  This protocol supports rendering strings, maps, functions, refs, files, seqs,
  input streams and URLs by default, and may be extended to cover many custom
  types."
  (render [this request]
    "Render the object into a form suitable for the given request map."))

(defn- guess-content-type [response name]
  (if-let [mime-type (mime/ext-mime-type (str name))]
    (response/content-type response mime-type)
    response))

(extend-protocol Renderable
  nil
  (render [_ _] nil)
  String
  (render [body _]
    (-> (response/response body)
        (response/content-type "text/html; charset=utf-8")))
  clojure.lang.APersistentMap
  (render [resp-map _]
    (merge (with-meta (response/response "") (meta resp-map))
           resp-map))
  clojure.lang.Fn
  (render [func request] (render (func request) request))
  clojure.lang.MultiFn
  (render [func request] (render (func request) request))
  clojure.lang.IDeref
  (render [ref request] (render (deref ref) request))
  java.io.File
  (render [file _]
    (-> (response/file-response (str file))
        (guess-content-type file)))
  clojure.lang.ISeq
  (render [coll _]
    (-> (response/response coll)
        (response/content-type "text/html; charset=utf-8")))
  java.io.InputStream
  (render [stream _] (response/response stream))
  java.net.URL
  (render [url _]
    (-> (response/url-response url)
        (guess-content-type url))))
