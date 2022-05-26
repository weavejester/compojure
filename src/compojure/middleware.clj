(ns compojure.middleware
  "Optional middleware to enhance routing in Compojure."
  (:require [compojure.core :refer [wrap-routes]]
            [ring.util.request :as req]
            [ring.util.response :as resp]

            [clojure.string :as str]))

(defn remove-trailing-slash
  "Remove the trailing '/' from the path of a request, if it exists."
  [request]
  (update request :uri #(if (str/ends-with? % "/")
                          (subs % 0 (dec (count %)))
                          %)))

(defn- redirect-to-canonical
  ([request]
   (resp/redirect (req/request-url request) 301))
  ([request respond raise]
   (respond (redirect-to-canonical request))))

(defn wrap-canonical-redirect
  "Middleware that permanently redirects any non-canonical route to its
  canonical equivalent, based on a make-canonical function that changes the URI
  in a request into its canonical form. If not supplied, the make-canonical function
  will default to [[remove-trailing-slash]]."
  ([handler]
   (wrap-canonical-redirect handler remove-trailing-slash))
  ([handler make-canonical]
   (let [redirect-handler (wrap-routes handler (constantly redirect-to-canonical))]
     (fn
       ([request & args]
        (let [canonical-request (make-canonical request)]
          (if (= request canonical-request)
            (apply handler request args)
            (apply redirect-handler canonical-request args))))))))
