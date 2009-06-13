;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.http.servlet
  "Functions for interfacing Compojure with the Java servlet standard."
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
  (:import javax.servlet.ServletContext)
  (:import org.apache.commons.io.IOUtils))

;; Functions to pull information from the request object

(defn- get-headers
  "Creates a name/value map of all the request headers."
  [#^HttpServletRequest request]
  (reduce
    (fn [headers name]
      (assoc headers (.toLowerCase name) (.getHeader request name)))
    {}
    (enumeration-seq (.getHeaderNames request))))

(defn- get-content-length
  "Returns the content length, or nil if there is no content."
  [#^HttpServletRequest request]
  (let [length (.getContentLength request)]
    (if (>= length 0)
      length)))

(defn create-request
  "Create the request map from the HttpServletRequest object."
  [#^HttpServletRequest request, #^HttpServlet servlet]
  {:server-port        (.getServerPort request)
   :server-name        (.getServerName request)
   :remote-addr        (.getRemoteAddr request)
   :uri                (.getRequestURI request)
   :query-string       (.getQueryString request)
   :scheme             (keyword (.getScheme request))
   :request-method     (keyword (.toLowerCase (.getMethod request)))
   :headers            (get-headers request)
   :content-type       (.getContentType request)
   :content-length     (get-content-length request)
   :character-encoding (.getCharacterEncoding request)
   :body               (.getInputStream request)
   ;; Custom non-Ring field:
   :servlet-request    request
   :servlet-context    (.getServletContext servlet)})

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
        (IOUtils/copy body out)
        (.close body)
        (.flush out))
    (instance? File body)
      (with-open [stream (FileInputStream. body)]
        (set-body response stream))))

(defn update-servlet-response
  "Update the HttpServletResponse using a response map."
  [#^HttpServletResponse response, {:keys [status headers body]}]
  (.setStatus  response status)
  (set-headers response headers)
  (set-body    response body))

;; Functions that combine request and response handling

(defn request-handler
  "Handle incoming HTTP requests from a servlet."
  [[servlet request response] routes]
  (.setCharacterEncoding response "UTF-8")
  (if-let [response-map (routes (create-request request servlet))]
    (update-servlet-response response response-map)
    (throw (NullPointerException. 
             "Handler returned nil (maybe no routes matched URI)"))))

(definline servlet
  "Create a servlet from a sequence of routes. Automatically updates if
  the routes binding is redefined."
  [routes]
  `(proxy [HttpServlet] []
     (~'service [request# response#]
       (request-handler [~'this request# response#]
         ~routes))))

(defmacro defservice
  "Defines a service method with an optional prefix suitable for being used by
  genclass to compile a HttpServlet class.
  e.g. (defservice my-routes)
       (defservice \"my-prefix-\" my-routes)"
  ([routes]
   `(defservice "-" ~routes))
  ([prefix routes]
   `(defn ~(symbol (str prefix "service"))
      [servlet# request# response#]
      (request-handler [servlet# request# response#]
        ~routes))))
