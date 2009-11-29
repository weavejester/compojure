(ns test.run
  (:use clojure.contrib.test-is)
  (:require test.compojure.crypto)
  (:require test.compojure.html.gen)
  (:require test.compojure.html.form-helpers)
  (:require test.compojure.html.page-helpers)
  (:require test.compojure.http.helpers)
  (:require test.compojure.http.middleware)
  (:require test.compojure.http.routes)
  (:require test.compojure.http.request)
  (:require test.compojure.http.response)
  (:require test.compojure.http.session)
  (:require test.compojure.str-utils)
  (:require test.compojure.validation))

(run-tests
  'test.compojure.crypto
  'test.compojure.http.helpers
  'test.compojure.http.middleware
  'test.compojure.http.routes
  'test.compojure.http.request
  'test.compojure.http.response
  'test.compojure.http.session
  'test.compojure.str-utils
  'test.compojure.validation)
