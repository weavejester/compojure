;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.http.helpers
  "Helper functions for things like redirection, serving files, 404s, etc."
  (:use compojure.encodings
        compojure.str-utils
        clojure.contrib.def
        clojure.contrib.str-utils
        clojure.contrib.duck-streams)
  (:import java.io.File))

(defn- encode-cookie
  "Encode sequence of key/value pairs a cookie."
  [name value attrs]
  (str-join "; "
    (cons (str (urlencode name) "=" (urlencode value))
          (for [[key val] attrs] (str* key "=" val)))))

(defn set-cookie
  "Return a Set-Cookie header."
  ([name value]
    {:headers {"Set-Cookie" (encode-cookie name value nil)}})
  ([name value & attrs]
    {:headers {"Set-Cookie" (encode-cookie name value (partition 2 attrs))}}))

(defn content-type
  "Retuns a Content-Type header given a type string."
  [type]
  {:headers {"Content-Type" type}})

(defn redirect-to
  "A shortcut for a '302 Moved' HTTP redirect."
  [location]
  [302 {:headers {"Location" location}}])
 
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
      #(.startsWith (.toLowerCase (.getName %)) "index.")
       (.listFiles dir))))

(defn safe-path?
  "Is a filepath safe for a particular root?"
  [root path]
  (.startsWith (.getCanonicalPath (File. root path))
               (.getCanonicalPath (File. root))))

(defn serve-file
  "Attempts to serve up a static file from a directory, which defaults to
  './public'. Nil is returned if the file does not exist. If the file is a
  directory, the function looks for a file in the directory called 'index.*'."
  ([path]
    (serve-file "public" path))
  ([root path]
    (let [filepath (File. root path)]
      (if (safe-path? root path)
        (cond 
          (.isFile filepath)
            filepath
          (.isDirectory filepath)
            (find-index-file filepath))))))
