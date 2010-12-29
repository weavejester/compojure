(ns compojure.handler
  (:use [ring.middleware params
                         keyword-params
                         nested-params
                         multipart-params
                         cookies
                         session]))

(defn api
  "Create a handler suitable for a web API."
  [routes]
  (-> routes
      wrap-keyword-params
      wrap-nested-params
      wrap-params))

(defn site
  "Create a handler suitable for a standard website."
  [routes]
  (-> (api routes)
      wrap-multipart-params
      wrap-session))
