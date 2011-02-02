(ns compojure.handler
  "Functions to create Ring handlers from routes."
  (:use [ring.middleware params
                         keyword-params
                         nested-params
                         multipart-params
                         cookies
                         session]))

(defn- with-opts [routes middleware opts]
  (if opts
    (middleware routes opts)
    (middleware routes)))

(defn api
  "Create a handler suitable for a web API. This adds the following
  middleware to your routes:
    - wrap-params
    - wrap-nested-params
    - wrap-keyword-params"
  [routes]
  (-> routes
      wrap-keyword-params
      wrap-nested-params
      wrap-params))

(defn site
  "Create a handler suitable for a standard website. This adds the
  following middleware to your routes:
    - wrap-session
    - wrap-cookies
    - wrap-multipart-params
    - wrap-params
    - wrap-nested-params
    - wrap-keyword-params

  A map of options may also be provided. These keys are provided:
    :session - a map of session middleware options"
  [routes & [opts]]
  (-> (api routes)
      (wrap-multipart-params)
      (with-opts wrap-session (:session opts))))
