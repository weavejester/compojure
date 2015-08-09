(ns compojure.middleware
  (:require [compojure.core :refer [wrap-routes]]
            [ring.util.response :as resp]))

(defn remove-trailing-slash [^String uri]
  (if (.endsWith uri "/")
    (.substring uri 0 (dec (.length uri)))
    uri))

(defn wrap-canonical-redirect [handler make-canonical]
  (fn [{uri :uri :as request}]
    (let [canonical (make-canonical uri)]
      (if (= canonical uri)
        (handler request)
        (resp/redirect canonical 301)))))
