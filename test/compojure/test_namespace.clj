(ns compojure.test-namespace
  "Just a test namespace")

(defn wrap-test2 [f]
  (fn [x] (f (* x x))))
