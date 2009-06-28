;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.http.middleware
  "Various middleware functions."
  (:use compojure.http.routes)
  (:use clojure.contrib.str-utils))

(defn header-option
  "Converts a header option KeyValue into a string."
  [[k v]]
  (cond 
    (true? v) 
      (name k)
    (false? v)
      ""
    :else
      (str (name k) "=" v)))

(defn header-options
  "Converts a map into an HTTP header options string."
  [m delim]
  (str-join delim 
    (filter 
      #(not (= "" %)) 
      (map header-option m))))

(defn with-headers
  "Merges a map of header name and values into the response.
   Will not overwrite existing headers."
  [handler headers]
  (fn [request]
    (let [response (handler request)
          merged-headers (merge headers (:headers response))]
      (assoc response :headers merged-headers))))

(defn with-cache-control
   "Middleware to set the Cache-Control http header.
    Map entries with boolean values either write their
    key's (name) if true, or nothing if false.
    Example:
    (... {:max-age 3600 :public false :must-revalidate true})
    Cache-Control: max-age=3600, must-revalidate"
   [handler m]
   (with-headers handler
     {"Cache-Control" (header-options m ", ")}))
