;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.http.request:
;;
;; Functions for pulling useful data out of a HTTP request map.

(ns compojure.http.request
  (:use compojure.control)
  (:use compojure.encodings)
  (:use compojure.map-utils)
  (:use compojure.str-utils)
  (:use clojure.contrib.duck-streams)
  (:use clojure.contrib.str-utils)
  (:import java.net.URLDecoder)
  (:import java.io.InputStreamReader))

(defn- parse-params
  "Parse parameters from a string into a map."
  [param-string separator]
  (reduce
    (fn [param-map s]
      (let [[key val] (re-split #"=" s)]
        (assoc-vec param-map
          (keyword (urldecode key))
          (urldecode (or val "")))))
    {}
    (remove blank?
      (re-split separator param-string))))

(defn get-query-params
  "Parse parameters from the query string."
  [request]
  (merge (request :query-params {})
    (if-let [query (request :query-string)]
      (parse-params query #"&"))))

(defn get-character-encoding
  "Get the character encoding, or use the default from duck-streams."
  [request]
  (or (request :character-encoding) *default-encoding*))

(defn- slurp-body
  "Slurp the request body into a string."
  [request]
  (let [encoding (get-character-encoding request)]
    (if-let [body (request :body)]
      (slurp* (InputStreamReader. body encoding)))))

(defn urlencoded-form?
  "Does a request have a urlencoded form?"
  [request]
  (if-let [type (:content-type request)]
    (.startsWith type "application/x-www-form-urlencoded")))

(defn get-form-params
  "Parse urlencoded form parameters from the request body."
  [request]
  (merge (request :form-params {})
    (if (urlencoded-form? request)
      (if-let [body (slurp-body request)]
        (parse-params body #"&")))))

(defn get-route-params
  "Get a map of the route parameters, or nil if not a map."
  [request]
  (let [params (request :route-params)]
    (if (map? params)
      params)))

(defn with-params
  "Decorator that adds urlencoded parameters to the request map. The following
  keys are added:
    :query-params
    :form-params
    :params"
  [handler]
  (fn [request]
    (let [query-params (get-query-params request)
          form-params  (get-form-params request)
          route-params (get-route-params request)
          params       (merge (request :params)
                              form-params
                              query-params
                              route-params)]
      (handler
        (merge request
          {:query-params query-params
           :form-params  form-params
           :params       params})))))

(defn get-cookies
  "Pull out a map of cookies from a request map."
  [request]
  (if-let [cookies (get-in request [:headers "cookie"])]
    (parse-params cookies #";\s*")))

(defn assoc-cookies
  "Associate cookies with a request map."
  [request]
  (assoc request :cookies (get-cookies request)))

(defn with-cookies
  "Decorator that adds cookies to a request map."
  [handler]
  (fn [request]
    (handler (assoc-cookies request))))
