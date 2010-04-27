(ns compojure.route
  (:use compojure.core
        [ring.util.response :only (file-response)]
        [ring.util.codec :only (url-decode)]))

(defn- add-wildcard
  "Add a wildcard to the end of a route path."
  [path]
  (str path (if (.endsWith path "/") "*" "/*")))

(defn files
  "A route for serving static files from a directory."
  [path & [options]]
  (GET (add-wildcard path) {{file-path "*"} :params}
    (let [options (merge {:root "public"} options)]
      (file-response file-path options))))

(defn not-found
  "A route that returns a 404 not found response."
  [body]
  (ANY "*" [] {:status 404, :body body}))
