(ns test.run
  (:use clojure.contrib.test-is)
  (:require test.compojure.html.gen))

(run-tests 
  'test.compojure.html.gen)
