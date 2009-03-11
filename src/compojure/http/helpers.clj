;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.http.helper:
;; 
;; Helper functions for things like redirection, serving files, 404s, etc.

(ns compojure.http.helpers
  (:use compojure.str-utils)
  (:use clojure.contrib.def)
  (:use clojure.contrib.duck-streams)
  (:import java.io.File)
  (:import java.net.URLEncoder))

(defn urlencode
  "Encode a urlencoded string using the default encoding."
  [s]
  (URLEncoder/encode (str* s) *default-encoding*))

(defn set-cookie
  "Return a Set-Cookie header."
  [name value]
  {"Set-Cookie" (str (urlencode name) "=" (urlencode value))})

(defn redirect-to
  "A shortcut for a '302 Moved' HTTP redirect."
  [location]
  [302 {"Location" location}])
 
(defn page-not-found
  "A shortcut to create a '404 Not Found' HTTP response."
  ([]
    (page-not-found "public/404.html"))
  ([filename]
    [404 (File. filename)]))

(defn- find-index-file
  "Search the directory for index.*"
  [dir]
  (first
    (filter
      #(.startsWith "index." (.toLowerCase (.getName %)))
       (.listFiles dir))))

(defn serve-file
  "Attempts to serve up a static file from a directory, which defaults to
  './public'. Nil is returned if the file does not exist. If the file is a
  directory, the function looks for a file in the directory called 'index.*'."
  ([path]
    (serve-file "public" path))
  ([root path]
    (let [filepath (File. root path)]
      (cond
        (.isFile filepath)
          filepath
        (.isDirectory filepath)
          (find-index-file filepath)))))
