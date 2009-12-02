(ns test.run
  (:use clojure.contrib.test-is)
  (:require test.compojure.routes)
  (:require test.compojure.http.helpers)
  (:require test.compojure.http.middleware)
  (:require test.compojure.http.request)
  (:require test.compojure.http.response)
  (:require test.compojure.str-utils))

(run-tests
  'test.compojure.routes
  'test.compojure.http.helpers
  'test.compojure.http.middleware
  'test.compojure.http.request
  'test.compojure.http.response)
