(ns test.compojure.http.response
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.response))

(defn random-status []
  (random-int 100 599))

(defn random-response []
  {:status  (random-status)
   :headers (random-map random-keyword random-str)
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

(fact "A string gets added to the response body"
  [body random-str]
  (= (:body (create-response body)) body))

(fact "Strings in a vector get concatenated together"
  [body #(random-vec random-str)]
  (= (:body (create-response body))
     (if (seq body)
       (apply str body))))

(fact "The last integer in a vector is used as the status code"
  [statuses #(random-vec random-status)]
  (= (:status (create-response statuses))
     (or (last statuses) 200)))
