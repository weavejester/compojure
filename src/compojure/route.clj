(ns compojure.route
  "Functions for defining common types of routes."
  (:require [compojure.response :as response]
            [compojure.core :refer [GET rfn]]
            [ring.util.mime-type :as mime]
            [ring.util.response :refer [file-response resource-response
                                        status content-type]]))

(defn- add-wildcard [^String path]
  (str path (if (.endsWith path "/") "*" "/*")))

(defn- add-mime-type [response path options]
  (if-let [mime-type (mime/ext-mime-type path (:mime-types options {}))]
    (content-type response mime-type)
    response))

(defn files
  "Returns a route for serving static files from a directory.

  Accepts the following options:

  :root
  : the root path where the files are stored, defaults to \"public\"

  :mime-types
  : an optional map of file extensions to mime types"
  ([path]
   (files path {}))
  ([path options]
   (GET (add-wildcard path) {{file-path :*} :route-params}
     (let [options  (merge {:root "public"} options)
           response (file-response file-path options)]
       (if response
         (add-mime-type response (str (:body response)) options))))))

(defn resources
  "Returns a route for serving resources on the classpath.

  Accepts the following options:

  :root
  : the root prefix path of the resources, defaults to \"public\"

  :mime-types
  : an optional map of file extensions to mime types"
  ([path]
   (resources path {}))
  ([path options]
   (GET (add-wildcard path) {{resource-path :*} :route-params}
     (let [root (:root options "public")]
       (some-> (resource-response (str root "/" resource-path))
               (add-mime-type resource-path options))))))

(defn not-found
  "Returns a route that always returns a 404 \"Not Found\" response with the
  supplied response body. The response body may be anything accepted by the
  [[response/render]] function."
  [body]
  (fn handler
    ([request]
     (-> (response/render body request)
         (status 404)
         (cond-> (= (:request-method request) :head) (assoc :body nil))))
    ([request respond raise]
     (respond (handler request)))))
