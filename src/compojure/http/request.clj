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
  (:use compojure.http.session)
  (:use compojure.map-utils)
  (:use compojure.str-utils)
  (:use clojure.contrib.duck-streams)
  (:use clojure.contrib.str-utils)
  (:import java.net.URLDecoder)
  (:import java.io.InputStreamReader))

(defn urldecode
  "Decode a urlencoded string using the default encoding."
  [s]
  (URLDecoder/decode s *default-encoding*))

(defn- parse-params
  "Parse parameters from a string into a map."
  [param-string separator]
  (reduce
    (fn [param-map s]
      (let [[key val] (re-split #"=" s)]
        (assoc-vec param-map
          (keyword (urldecode key))
          (urldecode val))))
    {}
    (remove blank?
      (re-split separator param-string))))

(defn get-query-params
  "Parse parameters from the query string."
  [request]
  (if-let [query (request :query-string)]
    (parse-params query #"&")
    {}))

(defn get-character-encoding
  "Get the character encoding, or use the default from duck-streams."
  [request]
  (or (request :character-encoding) *default-encoding*))

(defn slurp-body
  "Slurp the request body into a string."
  [request]
  (let [encoding (get-character-encoding request)]
    (if-let [body (request :body)]
      (slurp* (InputStreamReader. body encoding)))))

(defn get-form-params
  "Parse urlencoded form parameters from the request body."
  [request]
  (if (= (request :content-type) "application/x-www-form-urlencoded")
    (if-let [body (slurp-body request)]
      (parse-params body #"&")
      {})
    {}))

(defn assoc-parameters
  "Add urlencoded parameters to a request map."
  [request]
  (merge request
    {:query-params (get-query-params request)
     :form-params  (get-form-params request)}))

(defn get-route-params
  "Get a map of the route parameters, or nil if not a map."
  [request]
  (let [params (request :route-params)]
    (if (map? params)
      params)))

(defn get-params
  "Merge all parameters in the request map."
  [request]
  (merge
    (:query-params request)
    (:form-params request)
    (get-route-params request)))

(defn get-cookies
  "Pull out a map of cookies from a request map."
  [request]
  (if-let [cookies (get-in request [:headers "cookie"])]
    (parse-params cookies #";\s*")))

(defn assoc-cookies
  "Parse the cookies from a request map and add them back in under the
  :cookies key."
  [request]
  (assoc request :cookies (get-cookies request)))
