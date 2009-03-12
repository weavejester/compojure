(ns test.compojure.http.helpers
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.helpers))

(fact "A cookie can be set with set-cookie"
  [name  #"\w+"
   value #"\w+"]
  (let [response (set-cookie name value)]
    (= (get-in response [:headers "Set-Cookie"])
       (str name "=" value))))
