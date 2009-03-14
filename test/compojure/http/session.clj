(ns test.compojure.http.session
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.session))

(defn random-request []
  {:request-method :get
   :uri "/"})

(fact "The with-session wrapper adds a :session-id key to the request"
  [request random-request]
  (let [handler  (fn [req] {:body (req :session-id)})
        response ((with-session handler) request)]
    (not-empty (:body response))))

(fact "The with-session wrapper adds a Set-Cookie header"
  [request random-request]
  (let [handler  (fn [req] {})
        response ((with-session handler) request)]
    (contains? (:headers response) "Set-Cookie")))

(fact "You can set the global session store type with set-session-store!"
  [store-type #"\w{1,20}"]
  (set-session-store! store-type)
  (= *session-store* store-type))
