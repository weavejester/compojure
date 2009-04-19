;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure:
;; 
;; Convenience library that includes every compojure.* namespace. If you want
;; to access Compojure quickly, and don't care about having everything in one
;; namespace, just use or require 'compojure.

(ns compojure
  (:use compojure.ns-utils))

(immigrate
  'compojure.control
  'compojure.html.gen
  'compojure.html.page-helpers
  'compojure.html.form-helpers
  'compojure.http.helpers
  'compojure.http.multipart
  'compojure.http.routes
  'compojure.http.servlet
  'compojure.http.session
  'compojure.server.jetty
  'compojure.str-utils
  'compojure.map-utils
  'compojure.validation)
