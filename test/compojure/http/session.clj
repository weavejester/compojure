(ns test.compojure.http.session
  (:use compojure.http.session)
  (:use clojure.contrib.test-is))

;; Memory sessions

(deftest create-memory-session
  (binding [*session-store* :memory]
    (contains? (create-session) :id)))

(deftest memory-session-cookie
  (binding [*session-store* :memory]
    (let [session (create-session)]
      (is (= (session-cookie true session) (session :id)))
      (is (nil? (session-cookie false session))))))

(deftest read-memory-session
  (binding [*session-store* :memory
            memory-sessions (ref {::mock-id ::mock-session})]
    (is (= (read-session ::mock-id) ::mock-session))))

(deftest write-memory-session
  (binding [*session-store* :memory]
    (let [session (create-session)]
      (write-session session)
      (is (= (memory-sessions (session :id))
             session)))))

(deftest destroy-memory-sessions
  (let [mock-session {:id ::mock-id}]
    (binding [*session-store* :memory
              memory-sessions (ref {::mock-id mock-session})]
      (is (contains? @memory-sessions ::mock-id))
      (destroy-session mock-session)
      (is (not (contains? @memory-sessions ::mock-id))))))

;; Session routes

(deftest session-nil-response
  (let [handler  (with-session (constantly nil))
        response (handler {})]
    (is (nil? response))))

(defmethod create-session ::mock []
  {:id ::mock-id})

(defmethod write-session ::mock [session])

(defmethod read-session ::mock [id]
  (is (= id ::mock-id))
  {:id ::mock-id})

(defmethod session-cookie ::mock [new? session]
  "mock-session-cookie")

(defn- mock-session-response
  [request response]
  (let [handler (with-session (constantly response))]
    (handler request)))

(deftest new-session-cookie
  (binding [*session-store* ::mock]
    (let [response (mock-session-response {} {})]
      (is (= (get-in response [:headers "Set-Cookie"])
             "session=mock-session-cookie; path=/")))))

(deftest response-session-cookie
  (binding [*session-store* ::mock]
    (let [response (mock-session-response {} {:session {}})]
      (is (= (get-in response [:headers "Set-Cookie"])
             "session=mock-session-cookie; path=/")))))

(deftest session-store-test
  (set-session-store! ::mock)
  (is (= *session-store* ::mock))
  (set-session-store! :memory))
