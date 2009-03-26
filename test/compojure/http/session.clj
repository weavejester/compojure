(ns test.compojure.http.session
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.session))

(defn random-request []
  {:request-method :get
   :uri "/"})

(fact "Sessions are created with their id"
  [session #(create-session)]
  (contains? session :id))

(fact "Sessions can be written and read"
  [session #(create-session)
   update  #(random-map random-keyword random-str 1 10)]
  (let [updated-session (merge update session)]
    (write-session updated-session)
    (= (read-session (session :id))
       updated-session)))

(fact "Sessions can be destroyed"
  [session #(create-session)]
  (write-session session)
  (destroy-session session)
  (nil? (read-session (session :id))))

(fact "Sessions add a cookie if the response has the :session key."
  [request random-request]
  (let [handler  (with-session (constantly {:session {}}))
        response (handler request)]
    (contains? (:headers response) "Set-Cookie")))

(fact "You can set the global session store type with set-session-store!"
  [store-type #"\w{1,20}"]
  (set-session-store! store-type)
  (= *session-store* store-type)
  (set-session-store! :memory))

(fact "Sessions work with nil responses"
  [request random-request]
  (let [handler  (with-session (constantly nil))
        response (handler request)]
    (nil? response)))

(fact "A response with the :session key modifies the session")

(fact "Responses without :session keys do not modify the session")
