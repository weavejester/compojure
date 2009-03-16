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

(defmulti create-session  first-arg) ; [type]    -> id
(defmulti read-session    first-arg) ; [type id] -> session
(defmulti write-session   first-arg) ; [type id session]
(defmulti destroy-session first-arg) ; [type id]

;; In memory sessions

(def memory-sessions (ref {}))

(defmethod create-session :memory [_]
  (dosync
    (let [id (gen-uuid)]
      (alter memory-sessions assoc id (ref {}))
      id)))

(defmethod read-session :memory [_ id]
  (@memory-sessions id))

(defmethod write-session :memory [_ id session]
  (dosync
    (alter memory-sessions assoc id session)))

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
    (if (read-session *session-store* session-id)
      session-id
      (create-session *session-store*))))

(defn- set-session-id
  "Create a response with a session ID."
  [response session-id]
  (when-not (nil? response)
    (merge-response
      response
      (set-cookie :session-id session-id))))

(defn with-session
  "Wrap a handler in a session."
  [handler]
  (fn [request]
    (let [session-id (get-session-id request)
          session    (read-session *session-store* session-id)
          request    (assoc request
                       :session-id session-id
                       :session    session)
          response   (handler request)]
      (write-session *session-store* session-id (:session response))
      (set-session-id response session-id))))

(defn set-session
  "Return a response map with the session set."
  [session]
  {:session session})
