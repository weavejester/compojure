(ns test.compojure.http.session
  (:use compojure.http.session)
  (:use clojure.contrib.test-is))

;; Memory sessions

(deftest create-memory-session
  (is (= (keys (create-session {:type :memory}))
         [:id])))

(deftest memory-session-cookie
  (let [repo    {:type :memory}
        session (create-session repo)]
    (is (= (session-cookie repo true session) (session :id)))
    (is (nil? (session-cookie repo false session)))))

(deftest read-memory-session
  (binding [memory-sessions (ref {::mock-id ::mock-session})]
    (is (= (read-session {:type :memory} ::mock-id)
           ::mock-session))))

(deftest write-memory-session
  (binding [memory-sessions (ref {})]
    (let [session (create-session {:type :memory})]
      (write-session {:type :memory} session)
      (is (= (memory-sessions (session :id))
             session)))))

(deftest destroy-memory-sessions
  (let [mock-session {:id ::mock-id}]
    (binding [memory-sessions (ref {::mock-id mock-session})]
      (destroy-session {:type :memory} mock-session)
      (is (not (contains? @memory-sessions ::mock-id))))))

;; Cookie sessions

(deftest session-hmac-secret-key
  (is (= (session-hmac {:type :cookie, :secret-key "test"} "foobar")
         "ithiOBI7sp/MpMb9EXgxvm1gmufcQvFT+gRzIUiSd7A=")))

;; Associating session with request

(defmethod create-session ::mock [repository]
  {:id :new})

(defmethod read-session ::mock [repository id]
  {:id :current})

(deftest assoc-nil-session
  (let [request (assoc-session {:type ::mock} {})]
    (is (:new-session? request))
    (is (= (:session request) {:id :new}))))

(deftest assoc-expired-session
  (let [session {:expires-at (timestamp-after 0)}
        request (assoc-session {:type ::mock} {:session session})]
    (is (:new-session? request))
    (is (= (:session request) {:id :new}))))

(deftest assoc-existing-session
  (let [cookies {:compojure-session "current"}
        request (assoc-session {:type ::mock} {:cookies cookies})]
    (is (not (:new-session? request)))
    (is (= (:session request) {:id :current}))))
