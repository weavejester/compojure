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
  (:use compojure.file-utils)
  (:use compojure.http.routes)
  (:use compojure.http.request)
  (:import java.io.File)
  (:import java.io.InputStream)
  (:import java.net.URL)
  (:import java.util.Map$Entry)
  (:import javax.servlet.http.Cookie)
  (:import javax.servlet.http.HttpServlet)
  (:import javax.servlet.http.HttpServletRequest)
  (:import javax.servlet.http.HttpServletResponse)
  (:import javax.servlet.ServletContext))

;; Functions to pull information from the request object

(defn context-mimetype
  "Guess the mimetype of a filename. Defaults to 'application/octet-stream'
  if the mimetype is unknown."
  [#^ServletContext context filename]
  (or (.getMimeType context (str filename))
      "application/octet-stream"))

(defn get-headers
  "Creates a name/value map of all the request headers."
  [#^HttpServletRequest request]
  (reduce
    (fn [headers name]
      (assoc headers (.toLowerCase name) (.getHeader request name)))
    {}
    (enumeration-seq (.getHeaderNames request))))

(defn get-method
  "Returns either the value of the '_method' HTTP parameter, or the method
  of the HTTP request."
  [#^HttpServletRequest request]
  (keyword
    (.toLowerCase
      (or (.getParameter request "_method")
          (.getMethod request)))))

(defn get-content-length
  "Returns the content length, or nil if there is no content."
  [#^HttpServletRequest request]
  (let [length (.getContentLength request)]
    (if (>= length 0)
      length)))

(defn create-request-map
  "Create the request map from the HttpServletRequest object."
  [#^HttpServletRequest request]
  {:server-port        (.getServerPort request)
   :server-name        (.getServerName request)
   :remote-addr        (.getRemoteAddr request)
   :uri                (.getRequestURI request)
   :query-string       (.getQueryString request)
   :scheme             (keyword (.getScheme request))
   :request-method     (get-method request)
   :headers            (get-headers request)
   :content-type       (.getContentType request)
   :content-length     (get-content-length request)
   :character-encoding (.getCharacterEncoding request)
   :body               (.getInputStream request)})

;; Functions to set data in the response object

(defn- set-type-by-name
  "Set the content type header by guessing the mimetype from the resource name."
  [#^HttpServlet servlet, #^HttpServletResponse response, name]
  (.setHeader response "Content-Type"
    (context-mimetype (.getServletContext servlet) name)))

(defn update-response
  "Destructively update a HttpServletResponse using a Clojure datatype:
    string      - Adds to the response body
    seq         - Adds all containing elements to the response body
    map         - Updates the HTTP headers
    Cookie      - Adds a cookie to the response
    Number      - Updates the status code
    File        - Updates the response body via a file stream
    URL         - Updates the response body via a stream to the URL
    InputStream - Pipes the input stream to the resource body
    vector      - Iterates through its contents, successively updating the
                  response with each value"
  [#^HttpServlet servlet, #^HttpServletResponse response, update]
  (cond
    (vector? update)
      (doseq [u update]
        (update-response servlet response u))
    (string? update)
      (.. response (getWriter) (print update))
    (seq? update)
      (let [writer (.getWriter response)]
        (doseq [d update]
          (.print writer d)))
    (map? update)
      (doseq [[k v] update]
        (.setHeader response k v))
    (number? update)
      (.setStatus response update)
    (instance? Cookie update)
      (.addCookie response update)
    (instance? File update)
      (update-response servlet response (.toURL update))
    (instance? URL update)
      (do (set-type-by-name servlet response update)
          (update-response servlet response (.openStream update)))
    (instance? InputStream update)
      (with-open [in update]
        (copy-stream in (.getOutputStream response)))))

;; Functions that combine request and response handling

(defn- http-handler
  "Handle incoming HTTP requests from a servlet."
  [[servlet request response] routes]
  (do (.setCharacterEncoding response "UTF-8")
      (update-response servlet response
        (routes (create-request-map request)))))

(defn servlet
  "Create a servlet from a sequence of routes."
  [routes]
  (proxy [HttpServlet] []
    (service [request response]
       (http-handler [this request response] routes))))
