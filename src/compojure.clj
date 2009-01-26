(ns compojure
  (:use compojure.ns-utils))

;; Import all Compojure namespaces
(immigrate
  'compojure.control
  'compojure.file-utils
  'compojure.html
  'compojure.html.page-helpers
  'compojure.html.form-helpers
  'compojure.http.helpers
  'compojure.http.routes
  'compojure.http.servlet
  'compojure.json
  'compojure.server.jetty
  'compojure.str-utils
  'compojure.validation)
