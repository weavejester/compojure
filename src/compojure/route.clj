(ns compojure.route
  "Route functions that define common behavior."
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
  "A route for serving static files from a directory. Accepts the following
  keys:
    :root       - the root path where the files are stored, defaults to 'public'
    :mime-types - an optional map of file extensions to mime types"
  [path & [options]]
  (GET (add-wildcard path) {{file-path :*} :route-params}
    (let [options  (merge {:root "public"} options)
          response (file-response file-path options)]
      (if response
        (add-mime-type response (str (:body response)) options)))))

(defn resources
  "A route for serving resources on the classpath. Accepts the following
  keys:
    :root       - the root prefix path of the resources, defaults to 'public'
    :mime-types - an optional map of file extensions to mime types"
  [path & [options]]
  (GET (add-wildcard path) {{resource-path :*} :route-params}
    (let [root (:root options "public")]
      (some-> (resource-response (str root "/" resource-path))
              (add-mime-type resource-path options)))))

(defn not-found
  "A route that returns a 404 not found response, with its argument as the
  response body."
  [body]
  (rfn request
    (-> (response/render body request)
        (status 404))))
