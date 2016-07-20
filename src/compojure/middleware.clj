(ns compojure.middleware
  "Optional middleware to enhance routing in Compojure."
  (:require [compojure.core :refer [wrap-routes]]
            [ring.util.response :as resp]))

(defn remove-trailing-slash
  "Remove the trailing '/' from a URI string, if it exists."
  [^String uri]
  (if (.endsWith uri "/")
    (.substring uri 0 (dec (.length uri)))
    uri))

(defn- redirect-to-canonical
  ([request]
   (resp/redirect (:compojure/path request) 301))
  ([request respond raise]
   (respond (redirect-to-canonical request))))

(defn- assoc-path [request path]
  (assoc request :compojure/path path))

(defn wrap-canonical-redirect
  "Middleware that permanently redirects any non-canonical route to its
  canonical equivalent, based on a make-canonical function that changes a URI
  string into its canonical form. If not supplied, the make-canonical function
  will default to [[remove-trailing-slash]]."
  ([handler]
   (wrap-canonical-redirect handler remove-trailing-slash))
  ([handler make-canonical]
   (let [redirect-handler (wrap-routes handler (constantly redirect-to-canonical))]
     (fn
       ([{uri :uri :as request}]
        (let [canonical-uri (make-canonical uri)]
          (if (= uri canonical-uri)
            (handler request)
            (redirect-handler (assoc-path request canonical-uri)))))
       ([{uri :uri :as request} respond raise]
        (let [canonical-uri (make-canonical uri)]
          (if (= uri canonical-uri)
            (handler request respond raise)
            (redirect-handler (assoc-path request canonical-uri) respond raise))))))))
