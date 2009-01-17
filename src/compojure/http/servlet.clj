;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.http.servlet:
;; 
;; Functions for interfacing Compojure with the Java servlet standard.

(ns compojure.http.servlet
  (:use compojure.file-utils :only [copy-stream])
  (:import java.io.File)
  (:import java.io.InputStream)
  (:import java.net.URL)
  (:import java.util.Map$Entry)
  (:import javax.servlet.http.HttpServletRequest)
  (:import javax.servlet.http.HttpServletResponse))

(defn- parse-key-value
  "Parse key/value strings to make them more Clojure-friendly."
  [key val]
  [(keyword key)
   (if (rest val) val (first val))])

(defn get-params
  "Creates a name/value map of all the request parameters."
  [#^HttpServletRequest request]
  (apply hash-map
    (mapcat 
      (fn [#^Map$Entry e] (parse-key-value (.getKey e) (.getValue e)))
      (.getParameterMap request))))

(defn get-headers
  "Creates a name/value map of all the request headers."
  [#^HttpServletRequest request]
  (apply hash-map
    (mapcat
      #(parse-key-value (.toLowerCase %)
                        (enumeration-seq (.getHeaders request %)))
       (enumeration-seq (.getHeaderNames request)))))

(defn get-session
  "Returns a ref to a hash-map that acts as a HTTP session that can be updated
  within a Clojure STM transaction."
  [#^HttpServletRequest request]
  (let [session (.getSession request)]
    (or (.getAttribute session "clj-session")
        (let [clj-session (ref {})]
          (.setAttribute session "clj-session" clj-session)
          clj-session))))

(defn get-cookies
  "Creates a name/value map from all of the cookies in the request."
  [#^HttpServletRequest request]
  (apply hash-map
    (mapcat #(list (keyword (.getName %)) (.getValue %))
             (.getCookies request))))

(defn get-method
  "Returns either the value of the '_method' HTTP parameter, or the method
  of the HTTP request."
  [#^HttpServletRequest request]
  (or (.getParameter request "_method")
      (.getMethod request)))

(defn- set-type-by-name
  "Set the content type header by guessing the mimetype from the resource name."
  [#^HttpServletResponse response context name]
  (.setHeader response "Content-Type" (context-mimetype context name)))

(defn update-response
  "Destructively update a HttpServletResponse using a Clojure datatype:
    string      - Adds to the response body
    seq         - Adds all containing elements to the response body
    map         - Updates the HTTP headers
    Number      - Updates the status code
    File        - Updates the response body via a file stream
    URL         - Updates the response body via a stream to the URL
    InputStream - Pipes the input stream to the resource body
    vector      - Iterates through its contents, successively updating the
                  response with each value"
  [#^HttpServletResponse response context update]
  (cond
    (vector? update)
      (doseq [u update]
        (update-response response context u))
    (string? update)
      (.. response (getWriter) (print update))
    (seq? update)
      (let [writer (.getWriter response)]
        (doseq [d update]
          (.print writer d)))
    (map? update)
      (doseq [[k v] update]
        (.setHeader response k v))
    (number? Number update)
      (.setStatus response update)
    (instance? Cookie update)
      (.addCookie response update)
    (instance? File update)
      (update-response response context (.toURL update))
    (instance? URL update)
      (do (set-type-by-name response context (str update))
          (update-response response context (.openStream update)))
    (instance? InputStream update)
      (with-open [in update]
        (copy-stream in (.getOutputStream response)))))
