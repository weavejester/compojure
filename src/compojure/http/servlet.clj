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
  (:use [compojure.file-utils  :only (copy-stream)])
  (:use [compojure.http.routes :only (combine-routes)])
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

(defn- parse-key-value
  "Parse key/value strings to make them more Clojure-friendly."
  [key val]
  [(keyword key)
   (if (next val) val (first val))])

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

(defmacro with-servlet-vars
  "Adds local servlet vars to the scope given a HttpServlet and a
  HttpServletRequest instance."
  [[#^HttpServlet servlet, #^HttpServletRequest request] & body]
  `(let [~'context  (.getServletContext ~servlet)
         ~'method   (.getMethod   ~request)
         ~'url      (.getRequestURL ~request)
         ~'path     (.getPathInfo ~request)
         ~'params   (get-params   ~request)
         ~'headers  (get-headers  ~request)
         ~'mimetype (partial context-mimetype ~'context)
         ~'session  (get-session ~request)
         ~'cookies  (get-cookies ~request)]
      (do ~@body)))

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

;; Macros that combine request and response handling

(defmacro http-handler
  "Handle incoming HTTP requests from a servlet."
  [[servlet request response] & routes]
  `(do (.setCharacterEncoding ~response "UTF-8")
       (when-not (.getCharacterEncoding ~request)
         (.setCharacterEncoding ~request "UTF-8"))
       (update-response ~servlet ~response
         (with-servlet-vars [~servlet ~request]
           ((combine-routes ~@routes)
              (get-method ~request)
              (.getPathInfo ~request))))))

(defmacro servlet
  "Create a servlet from a sequence of routes."
  [& routes]
  `(proxy [HttpServlet] []
     (service ~'[request response]
       (http-handler ~'[this request response]
         ~@routes))))

(defmacro update-servlet
  "Update an existing servlet proxy with a new set of routes."
  [object & routes]
  `(update-proxy ~object
     {"service" (fn ~'[this request response]
                  (http-handler ~'[this request response]
                    ~@routes))}))

(defmacro defservlet
  "Defines a new servlet with an optional doc-string, or if a servlet is
  already defined, it updates the existing servlet with the supplied handlers.
  Note that updating is not a thread-safe operation."
  [name doc & handlers]
  (if (string? doc)
    `(do (defonce
          ~(with-meta name (assoc (meta name) :doc doc))
           (proxy [HttpServlet] []))
         (update-servlet ~name ~@handlers))
    `(do (defonce ~name
           (proxy [HttpServlet] []))
         (update-servlet ~name ~doc ~@handlers))))

(defmacro defservice
  "Defines a service method with an optional prefix suitable for being used by
  genclass to compile a HttpServlet class.
  e.g. (defservice
         (GET \"/\" \"Hello World\"))
       (defservice \"myprefix-\"
         (GET \"/\" \"Hello World\"))"
  [prefix & routes]
  (let [[prefix routes] (if (string? prefix)
                           [prefix routes]
                           ["-" (cons prefix routes)])]
    `(defn ~(symbol (str prefix "service"))
     ~'[this request response]
       (http-handler ~'[this request response]
         ~@routes))))
