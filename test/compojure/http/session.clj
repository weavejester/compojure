(ns test.compojure.http.session
  (:use compojure.http.session)
  (:use clojure.contrib.test-is))

;; Memory sessions

(deftest create-memory-session
  (contains? (create-session {:type :memory}) :id))

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
  {:id ::mock-id})

(deftest assoc-session-from-nil
  (let [request (assoc-session {:type ::mock} {})]
    (is (:new-session? request))
    (is (= (:session request) {:id ::mock-id}))))

;; Session routes

(comment
(deftest session-nil-response
  (let [handler  (with-session (constantly nil))
        response (handler {})]
    (is (nil? response))))

(defmethod create-session ::mock [repository]
  {:id ::mock-id})

(defmethod write-session ::mock [repository session])

(defmethod read-session ::mock [repository id]
  (is (= id ::mock-id))
  {:id ::mock-id})

(defmethod session-cookie ::mock [repository new? session]
  "mock-session-data")

(defn- mock-session-response [response]
  (let [handler (-> (constantly response) (with-session ::mock))]
    (handler {})))

(deftest new-session-cookie
  (let [response (mock-session-response {})]
    (is (= (get-in response [:headers "Set-Cookie"])
           "compojure-session=mock-session-data; path=/"))))

(deftest response-session-cookie
  (let [response (mock-session-response {:session {}})]
    (is (= (get-in response [:headers "Set-Cookie"])
           "compojure-session=mock-session-data; path=/"))))

(declare mock-store)

(derive ::mock-update ::mock)

(defmethod write-session ::mock-update [session]
  (set! mock-store session))

(defmethod read-session ::mock-update [id]
  mock-store)

(defn- mock-session-update [request response]
  (let [handler (-> (constantly response) (with-session ::mock-update))]
    (handler request)))

(deftest session-write-new
  (binding [mock-store nil]
    (mock-session-update {} {:session {:foo "bar"}})
    (is (= mock-store {:foo "bar"}))))

(deftest session-write-update
  (binding [mock-store {:foo "bar"}]
    (mock-session-update {} {:session {:foo "baz"}})
    (is (= mock-store {:foo "baz"}))))

(deftest session-write-no-update
  (binding [mock-store {:foo "bar"}]
    (mock-session-update {:cookies {:compojure-session "mock-id"}} {})
    (is (= mock-store {:foo "bar"}))))

(declare deleted-mock)

(derive ::mock-delete ::mock)

(defmethod destroy-session ::mock-delete [session]
  (set! deleted-mock session))

(deftest session-destroy-existing
  (binding [deleted-mock nil]
    (let [handler (-> (constantly {:session nil})
                      (with-session ::mock-delete))]
      (handler {})
      (is (= deleted-mock {:id ::mock-id})))))

)
