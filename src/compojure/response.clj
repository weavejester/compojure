(ns compojure.response
  "A protocol for generating Ring response maps"
  (:refer-clojure :exclude [send])
  (:require [ring.util.mime-type :as mime]
            [ring.util.response :as response]))

(defprotocol Renderable
  "A protocol that tells Compojure how to handle the return value of routes
  defined by [[GET]], [[POST]], etc.

  This protocol supports rendering strings, maps, functions, refs, files, seqs,
  input streams and URLs by default, and may be extended to cover many custom
  types."
  (render [x request]
    "Render `x` into a form suitable for the given request map."))

(defprotocol Sendable
  "A protocol that tells Compojure how to handle the return value of
  asynchronous routes, should they require special attention."
  (send* [x request respond raise]))

(defn send
  "Send `x` as a Ring response. Checks to see if `x` satisfies [[Sendable]],
  and if not, falls back to [[Renderable]]."
  [x request respond raise]
  (if (satisfies? Sendable x)
    (send* x request respond raise)
    (respond (render x request))))

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

(extend-protocol Sendable
  clojure.lang.Fn
  (send* [func request respond raise]
    (func request #(send % request respond raise) raise))
  clojure.lang.MultiFn
  (send* [func request respond raise]
    (func request #(send % request respond raise) raise)))
