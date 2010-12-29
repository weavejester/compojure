(ns compojure.handler
  "Functions to create Ring handlers from routes."
  (:use [ring.middleware params
                         keyword-params
                         nested-params
                         multipart-params
                         cookies
                         session]))

(defn- with-opts
  [routes middleware opts]
  (if opts
    (middleware routes opts)
    (middleware routes)))

(defn api
  "Create a handler suitable for a web API."
  [routes]
  (-> routes
      wrap-keyword-params
      wrap-nested-params
      wrap-params))

(defn site
  "Create a handler suitable for a standard website."
  [routes & [opts]]
  (-> (api routes)
      (wrap-multipart-params)
      (with-opts wrap-session (:session opts))))
