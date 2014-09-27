(ns compojure.route
  "Route functions that define common behavior."
  (:require [compojure.response :as response]
            [compojure.core :refer [GET rfn]]
            [ring.util.response :refer [file-response resource-response status]]
            [ring.middleware.content-type :refer [wrap-content-type]]))

(defn- add-wildcard
  "Add a wildcard to the end of a route path."
  [^String path]
  (str path (if (.endsWith path "/") "*" "/*")))

(defn files
  "A route for serving static files from a directory. Accepts the following
  keys:
    :root - the root path where the files are stored. Defaults to 'public'."
  [path & [options]]
  (-> (GET (add-wildcard path) {{file-path :*} :route-params}
        (let [options (merge {:root "public"} options)]
          (file-response file-path options)))
      (wrap-content-type options)))

(defn resources
  "A route for serving resources on the classpath. Accepts the following
  keys:
    :root - the root prefix to get the resources from. Defaults to 'public'."
  [path & [options]]
  (-> (GET (add-wildcard path) {{resource-path :*} :route-params}
        (let [root (:root options "public")]
          (resource-response (str root "/" resource-path))))
      (wrap-content-type options)))

(defn not-found
  "A route that returns a 404 not found response, with its argument as the
  response body."
  [body]
  (rfn request
    (-> (response/render body request)
        (status 404))))
