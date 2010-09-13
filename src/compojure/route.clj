(ns compojure.route
  (:use compojure.core
        [ring.util.response :only (file-response resource-response)]
        [ring.util.codec :only (url-decode)]))

(defn- add-wildcard
  "Add a wildcard to the end of a route path."
  [path]
  (str path (if (.endsWith path "/") "*" "/*")))

(defn files
  "A route for serving static files from a directory. Accepts the following
  keys:
    :root - the root path where the files are stored. Defaults to 'public'."
  [path & [options]]
  (GET (add-wildcard path) {{file-path "*"} :params}
    (let [options (merge {:root "public"} options)]
      (file-response file-path options))))

(defn resources
  "A route for serving resources on the classpath. Accepts the following
  keys:
    :root - the root prefix to get the resources from. Defaults to 'public'."
  [path & [options]]
  (GET (add-wildcard path) {{resource-path "*"} :params}
    (let [options (merge {:root "public"} options)]
      (resource-response resource-path options))))

(defn not-found
  "A route that returns a 404 not found response, with its argument as the
  response body."
  [body]
  (ANY "*" [] {:status 404, :body body}))
