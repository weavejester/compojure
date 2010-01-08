(ns compojure.http.session-test
  (:use compojure.crypto
        compojure.encodings
        compojure.http.session
        clojure.contrib.test-is)
  (:import javax.crypto.spec.IvParameterSpec
           javax.crypto.spec.SecretKeySpec))

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

(deftest create-cookie-session
  (is (= (create-session {:type :cookie}) {})))

;; Associating session with request

(defmethod create-session ::mock [repository]
  {:id :new})

(defmethod read-session ::mock [repository id]
  {:id :current})

(deftest assoc-nil-session
  (let [request (assoc-session {} {:type ::mock})]
    (is (:new-session? request))
    (is (= (:session request) {:id :new}))))

(deftest assoc-expired-session
  (let [session {:expires-at (timestamp-after 0)}
        request (assoc-session {:session session} {:type ::mock})]
    (is (:new-session? request))
    (is (= (:session request) {:id :new}))))

(deftest assoc-existing-session
  (let [cookies {:compojure-session "current"}
        request (assoc-session {:cookies cookies} {:type ::mock})]
    (is (not (:new-session? request)))
    (is (= (:session request) {:id :current}))))

(deftest assoc-flash-data
  (let [session {:flash {:message "test"}}
        request (assoc-flash {:session session})]
    (is (not (contains? (request :session) :flash)))
    (is (= (request :flash) {:message "test"}))))
