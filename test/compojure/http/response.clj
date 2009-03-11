(ns test.compojure.http.response
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.response))

(fact "A nil value generates a blank '200 OK' response"
  []
  (= (create-response nil)
     {:status 200, :headers {}}))

(fact "An integer value sets the response status code"
  [status #(random-int 100 599)]
  (= (:status (create-response status))
     status))
