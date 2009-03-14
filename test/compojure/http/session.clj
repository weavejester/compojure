(ns test.compojure.http.session
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.session))

(defn random-request []
  {:request-method :get
   :uri "/"})

(fact "The with-session wrapper adds a :session-id key to the request"
  [request random-request]
  (let [handler  (fn [request]
                   {:body (contains? request :session-id)})
        response ((with-session handler) request)]
    (true? (:body response))))
