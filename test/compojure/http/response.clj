(ns test.compojure.http.response
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.response))

(defn random-status []
  (random-int 100 599))

(defn random-response []
  {:status  (random-status)
   :headers {}
   :body    (random-str)})

(fact "A nil value generates a blank '200 OK' response"
  []
  (= (create-response nil)
     {:status 200, :headers {}}))

(fact "An integer value sets the response status code"
  [status random-status]
  (= (:status (create-response status))
     status))

(fact "The :next keyword generates a nil response"
  []
  (nil? (create-response :next)))

(fact "A response map gets passed straight through"
  [response random-response]
  (= (create-response response) response))

(fact "A vector of responses get merged together"
  [responses #(random-seq random-response)]
  (= (create-response (vec responses))
     (last responses)))
