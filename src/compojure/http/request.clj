;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.http.request:
;;
;; Functions for pulling useful data out of a the HTTP request.

; NOTE:
; Eventually these functions will parse the data directly from the request map,
; but for now lets cheat and sneakily pull the parameters from the
; HttpServletRequest object stored in (request :servlet-request).

(ns compojure.http.request
  (:use compojure.control))

(defn- parse-key-value
  "Parse key/value strings to make them more Clojure-friendly."
  [key val]
  [(keyword key)
   (if (next val) (vec val) (first val))])

(defn get-params
  "Retrieve a map of parameters from the request map."
  [request]
  (into {}
    (map #(parse-key-value (key %) (val %))
          (maybe .getParameterMap (request :servlet-request)))))

(defn get-cookies
  "Retrieve a map of cookies from the request map."
  [request]
  (into {}
    (map #(list (keyword (.getName %))
                (.getValue %))
          (maybe .getCookies (request :servlet-request)))))

(defn get-session
  "Returns a ref to a hash-map that acts as a HTTP session that can be updated
  within a Clojure STM transaction."
  [request]
  (let [session (maybe .getSession (request :servlet-request))]
    (or (maybe .getAttribute session "clj-session")
        (let [clj-session (ref {})]
          (maybe .setAttribute session "clj-session" clj-session)
          clj-session))))

(defmacro with-request-bindings
  "Add shortcut bindings for the keys in a request map."
  [req & body]
  `(let [~'request ~req
         ~'headers (:headers ~req)
         ~'params  (get-params ~req)
         ~'cookies (get-cookies ~req)
         ~'session (get-session ~req)]
     ~@body))
