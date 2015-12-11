(ns compojure.middleware
  (:require [compojure.core :refer [wrap-routes]]
            [ring.util.response :as resp]))

(defn remove-trailing-slash
  "Remove the trailing '/' from a URI string, if it exists."
  [^String uri]
  (if (.endsWith uri "/")
    (.substring uri 0 (dec (.length uri)))
    uri))

(defn- redirect-to-uri [{:keys [uri]}]
  (resp/redirect uri 301))

(defn wrap-canonical-redirect
  "Middleware that permanently redirects any non-canonical route to its canonical
  equivalent, based on a make-canonical function that changes a URI string into
  its canonical form. If not supplied, make-canonical function will default to
  remove-trailing-slash."
  ([handler]
   (wrap-canonical-redirect handler remove-trailing-slash))
  ([handler make-canonical]
   (let [redirect-handler (wrap-routes handler (constantly redirect-to-uri))]
     (fn [{uri :uri :as request}]
       (let [canonical-uri (make-canonical uri)]
         (if (= uri canonical-uri)
           (handler request)
           (redirect-handler (assoc request :uri canonical-uri))))))))
