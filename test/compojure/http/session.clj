(ns test.compojure.http.session
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.session))

(defn random-request []
  {:request-method :get
   :uri "/"})

(fact "The with-session wrapper adds a :session-id key to the request"
  [request random-request]
  (let [handler  (fn [req] {:body (contains? req :session-id)})
        response ((with-session handler) request)]
    (true? (:body response))))

(fact "The with-session wrapper adds a Set-Cookie header"
  [request random-request]
  (let [handler  (fn [req] {})
        response ((with-session handler) request)]
    (contains? (:headers response) "Set-Cookie")))
