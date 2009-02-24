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
  (:import java.io.FileInputStream)
  (:import java.util.Map$Entry)
  (:import javax.servlet.http.Cookie)
  (:import javax.servlet.http.HttpServlet)
  (:import javax.servlet.http.HttpServletRequest)
  (:import javax.servlet.http.HttpServletResponse)
  (:import javax.servlet.ServletContext))

;; Functions to pull information from the request object

(defn- get-headers
  "Creates a name/value map of all the request headers."
  [#^HttpServletRequest request]
  (reduce
    (fn [headers name]
      (assoc headers (.toLowerCase name) (.getHeader request name)))
    {}
    (enumeration-seq (.getHeaderNames request))))

(defn- get-method
  "Returns either the value of the '_method' HTTP parameter, or the method
  of the HTTP request."
  [#^HttpServletRequest request]
  (keyword
    (.toLowerCase
      (or (.getParameter request "_method")
          (.getMethod request)))))

(defn- get-content-length
  "Returns the content length, or nil if there is no content."
  [#^HttpServletRequest request]
  (let [length (.getContentLength request)]
    (if (>= length 0)
      length)))

(defn create-request
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
   :body               (.getInputStream request)
   ;; Custom non-Ring field:
   :servlet-request    request})

;; Functions to set data in the response object

(defn- set-headers
  "Update a HttpServletResponse with a map of headers."
  [#^HttpServletResponse response, headers]
  (doseq [[key val-or-vals] headers]
    (if (string? val-or-vals)
      (.setHeader response key val-or-vals)
      (doseq [val val-or-vals]
        (.addHeader response key val))))
  ; Some headers must be set through specific methods
  (when-let [content-type (get headers "Content-Type")]
    (.setContentType response content-type)))

(defn- set-body
  "Update a HttpServletResponse body with a String, ISeq, File or InputStream."
  [#^HttpServletResponse response, body]
  (cond
    (string? body)
      (with-open [writer (.getWriter response)]
        (.println writer body))
    (seq? body)
      (with-open [writer (.getWriter response)]
        (doseq [chunk body]
          (.print writer (str chunk))))
    (instance? InputStream body)
      (with-open [out (.getOutputStream response)]
        (copy-stream body out)
        (.close body)
        (.flush out))
    (instance? File body)
      (with-open [stream (FileInputStream. body)]
        (set-body stream))))

(defn update-response
  "Update the HttpServletResponse using a response map."
  [#^HttpServletResponse response, {:keys [status headers body]}]
  (.setStatus  response status)
  (set-headers response headers)
  (set-body    response body))

;; Functions that combine request and response handling

(defn- http-handler
  "Handle incoming HTTP requests from a servlet."
  [[servlet request response] routes]
  (do (.setCharacterEncoding response "UTF-8")
      (update-response response
        (routes (create-request request)))))

(defn servlet
  "Create a servlet from a sequence of routes."
  [routes]
  (proxy [HttpServlet] []
    (service [request response]
       (http-handler [this request response] routes))))
