(ns test.compojure.http.session
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.session))

(defn random-request []
  {:request-method :get
   :uri "/"})

(fact "Sessions are created with their id"
  [session #(create-session :memory)]
  (contains? session :id))

(fact "Sessions can be written and read"
  [session #(create-session :memory)
   update  #(random-map random-keyword random-str 1 10)]
  (let [updated-session (merge update session)]
    (write-session :memory updated-session)
    (= (read-session :memory (session :id))
       updated-session)))

(fact "Sessions can be destroyed"
  [session #(create-session :memory)]
  (write-session :memory session)
  (destroy-session :memory session)
  (nil? (read-session :memory (session :id))))

(fact "The with-session wrapper adds a Set-Cookie header"
  [request random-request]
  (let [handler  (with-session (constantly {}))
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
