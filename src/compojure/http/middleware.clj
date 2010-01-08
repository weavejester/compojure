;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.http.middleware
  "Various middleware functions."
  (:use compojure.http.routes
        compojure.str-utils
        clojure.contrib.def
        clojure.contrib.str-utils))

(defn header-option
  "Converts a header option KeyValue into a string."
  [[key val]]
  (cond 
    (true? val)  (str* key)
    (false? val) nil
    :otherwise   (str* key "=" val)))

(defn header-options
  "Converts a map into an HTTP header options string."
  [m delimiter]
  (str-join delimiter
    (remove nil? (map header-option m))))

(defn with-headers
  "Merges a map of header name and values into the response.  Overwrites 
   existing headers."
  [handler headers]
  (fn [request]
    (if-let [response (handler request)]
      (assoc response :headers
             (merge (:headers response) headers)))))

(defn with-cache-control
   "Middleware to set the Cache-Control http header. Map entries with boolean
   values either write their key if true, or nothing if false.
   Example:
   {:max-age 3600 :public false :must-revalidate true}
   => Cache-Control: max-age=3600, must-revalidate"
   [handler header-map]
   (with-headers handler
     {"Cache-Control" (header-options header-map ", ")}))

(defn with-uri-rewrite
  "Rewrites a request uri with the result of calling f with the
   request's original uri.  If f returns nil the handler is not called."
  [handler f]
  (fn [request]
    (let [uri (:uri request)
          rewrite (f uri)]
      (if rewrite
        (handler (assoc request :uri rewrite))
        nil))))

(defn- remove-or-nil-context
  "Removes a context string from the front of a uri.  If it wasn't there,
   returns nil."
  [uri context]
  (if (.startsWith uri context)
    (if-not (= uri context)
      (subs uri (count context))
      "/")
    nil))

(defn with-context
  "Removes the context string from the beginning of the request uri
   such that route matching is done without it.  If the context is not
   present, the handler will not be called."
  [handler context]
  (with-uri-rewrite handler #(remove-or-nil-context % context)))

(defn- uri-snip-slash
  "Removes a trailing slash from all uris except \"/\"."
  [uri]
  (if (and (not (= "/" uri))
           (.endsWith uri "/"))
    (chop uri)
    uri))

(defn ignore-trailing-slash
  "Makes routes match regardless of whether or not a uri ends in a slash."
  [handler]
  (with-uri-rewrite handler uri-snip-slash))

(defvar default-mimetypes
  {"css"  "text/css"
   "gif"  "image/gif"
   "gz"   "application/gzip"
   "htm"  "text/html"
   "html" "text/html"
   "jpg"  "image/jpeg"
   "js"   "text/javascript"
   "pdf"  "application/pdf"
   "png"  "image/png"
   "swf"  "application/x-shockwave-flash"
   "txt"  "text/plain"
   "xml"  "text/xml"
   "zip"  "application/zip"}
  "Default mimetype map used by with-mimetypes.")

(defn- extension
  "Returns the text after the last . of a String or nil."
  [s]
  (second (re-find #"\.(.*$)" s)))

(defn- request-mimetype
  "Derives the mimetype from a request.  See with-mimetypes for options."
  [request options]
  (let [default (or (:default options) "text/html")]
    (if-let [ext (extension (:uri request))]
      (let [mimetypes (or (:mimetypes options) default-mimetypes)]
        (get mimetypes ext default))
      default)))

(defn with-mimetypes
  "Middleware to add the proper Content-Type header based on the uri of
   the request.  options is a map containing a :mimetype map of extension
   to type and a :default mime type.  If :mimetype is not provided, a default
   map with common mime types will be used.  If :default is not provided,
   \"text/html\" is used."
  ([handler]
     (with-mimetypes handler {}))
  ([handler options]
     (fn [request]
       (let [mimetype (request-mimetype request options)]
         ((with-headers handler {"Content-Type" mimetype}) request)))))
