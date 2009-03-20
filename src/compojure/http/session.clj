;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.http.session:
;;
;; Functions for creating and updating HTTP sessions.

(ns compojure.http.session
  (:use compojure.str-utils)
  (:use compojure.http.helpers)
  (:use compojure.http.response))

;; Override these mulitmethods to create your own session storage

(defn- first-arg [x & _] x)

(defmulti create-session  first-arg) ; [type]    -> session
(defmulti read-session    first-arg) ; [type id] -> session
(defmulti write-session   first-arg) ; [type session]
(defmulti destroy-session first-arg) ; [type session]

;; In memory sessions

(def memory-sessions (ref {}))

(defmethod create-session :memory [_]
  {:id (gen-uuid)})

(defmethod read-session :memory [_ id]
  (@memory-sessions id))

(defmethod write-session :memory [_ session]
  (dosync
    (alter memory-sessions
      assoc (session :id) session)))

(defmethod destroy-session :memory [_ session]
  (dosync
    (alter memory-sessions
      dissoc (session :id))))

;; General methods for handling sessions

(def *session-store* :memory)

(defn set-session-store!
  "Set the global session store type (defaults to :memory)."
  [store]
  (def *session-store* store))

(defn- get-request-session
  "Retrieve the session using the 'session-id' cookie in the request."
  [request]
  (if-let [session-id (get-in request [:cookies :session-id])]
    (read-session *session-store* session-id)))

(defn- assoc-request-session
  "Associate the session with the request."
  [request]
  (if-let [session (get-request-session request)]
    (assoc request
      :session session)
    (assoc request
      :session      (create-session *session-store*)
      :new-session? true)))

(defn- set-session-cookie
  "Set the session cookie for the response."
  [response session-id]
  (merge-with merge response (set-cookie :session-id session-id)))

(defn- write-response-session
  "Save the session in the response."
  [session id]
  (let [session (assoc session :id id)]
    (write-session *session-store* session)))

(defn with-session
  "Wrap a handler in a session."
  [handler]
  (fn [request]
    (let [request    (assoc-request-session request)
          session-id (-> request :session :id)
          response   (handler request)]
      (when-not (nil? response)
        (if-let [session (response :session)]
          (write-response-session session session-id))
        (if (request :new-session?)
          (set-session-cookie response session-id)
          response)))))

(defn set-session
  "Return a response map with the session set."
  [session]
  {:session session})

(defn session-assoc
  "Associate key value pairs with the session."
  [session & keyvals]
  (set-session (apply assoc session keyvals)))

(defn session-dissoc
  "Dissociate keys from the session."
  [session & keys]
  (set-session (apply dissoc session keys)))
