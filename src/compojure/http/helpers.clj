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

(ns compojure.http.helper
  (:use compojure.str-utils :only [str*])
  (:import java.io.File))

(defn redirect-to
  "A shortcut for a '302 Moved' HTTP redirect."
  [location]
  [302 {"Location" location}])
 
(defn page-not-found
  "A shortcut to create a '404 Not Found' HTTP response."
  ([]
    (page-not-found "public/404.html"))
  ([filename]
    [404 (file filename)]))
 
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

(defn new-cookie
  "Helper function for creating new Cookie objects."
  [name value & attrs]
  (let [cookie    (new Cookie (str* name) value)
        attrs     (apply hash-map attrs)
        set-attr #(if (attrs %1)
                    (eval (list %2 cookie (attrs %1))))]
    (set-attr :comment '.setComment)
    (set-attr :domain  '.setDomain)
    (set-attr :max-age '.setMaxAge)
    (set-attr :path    '.setPath)
    (set-attr :secure  '.setSecure)
    (set-attr :version '.setVersion)
    cookie)) 
