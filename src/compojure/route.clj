(ns compojure.route
  (:require [compojure.response :as response])
  (:use compojure.core
        [ring.util.response :only (file-response resource-response status)]
        [ring.util.codec :only (url-decode)]
        ring.middleware.content-type
        ring.middleware.file-info))

(defn- add-wildcard
  "Add a wildcard to the end of a route path."
  [path]
  (str path (if (.endsWith path "/") "*" "/*")))

(defn files
  "A route for serving static files from a directory. Accepts the following
  keys:
    :root - the root path where the files are stored. Defaults to 'public'."
  [path & [options]]
  (-> (GET (add-wildcard path) {{file-path :*} :route-params}
        (let [options (merge {:root "public"} options)]
          (file-response file-path options)))
      (wrap-file-info (:mime-types options))))

(defn resources
  "A route for serving resources on the classpath. Accepts the following
  keys:
    :root - the root prefix to get the resources from. Defaults to 'public'."
  [path & [options]]
  (-> (GET (add-wildcard path) {{resource-path :*} :route-params}
        (let [root (:root options "public")]
          (resource-response (str root "/" resource-path))))
      (wrap-file-info (:mime-types options))
      (wrap-content-type options)))

(defn not-found
  "A route that returns a 404 not found response, with its argument as the
  response body."
  [body]
  (routes
    (HEAD "*" [] {:status 404})
    (ANY "*" {:as request}
      (-> (response/render body request)
          (status 404)))))
