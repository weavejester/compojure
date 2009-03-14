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

(defmulti create-session  (fn [type] type))
(defmulti get-session     (fn [type id] type))
(defmulti destroy-session (fn [type id] type))

;; In memory sessions

(def memory-sessions (ref {}))

(defmethod create-session :memory [_]
  (dosync
    (let [id (gen-uuid)]
      (alter memory-sessions assoc id (ref {}))
      id)))

(defmethod get-session :memory [_ id]
  (@memory-sessions id))

(defmethod destroy-session :memory [_ id]
  (dosync
    (alter memory-sessions dissoc id)))

;; General methods for handling sessions

(def *session-store* :memory)

(defn set-session-store!
  "Set the global session store type (defaults to :memory)."
  [store]
  (def *session-store* store))

(defn- get-session-id
  "Get the session ID from the request or create a new session."
  [request]
  (let [session-id (get-in request [:cookies :session-id])]
    (if (get-session *session-store* session-id)
      session-id
      (create-session *session-store*))))

(defn- set-session-id
  "Create a response with a session ID."
  [response session-id]
  (merge-response
    response
    (set-cookie :session-id session-id)))

(defn with-session
  "Wrap a handler in a session."
  [handler]
  (fn [request]
    (let [session-id (get-session-id request)
          request    (assoc request :session-id session-id)
          response   (handler request)]
      (set-session-id response session-id))))

(defn get-request-session
  "Get a session map via a request augmented by with-session."
  [request]
  (get-session *session-store* (:session-id request)))
