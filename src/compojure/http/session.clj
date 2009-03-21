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

;; Global session store type

(def *session-store* :memory)

(defn set-session-store!
  "Set the global session store type (defaults to :memory)."
  [store]
  (def *session-store* store))

;; Override these mulitmethods to create your own session storage

(defmulti create-session
  "Create a session map that's blank except for an :id. Should not attempt to
  save the session."
  (fn [type] type))
  
(defmulti read-session
  "Read in the session matching the ID from the session store and return the
  session map."
  (fn [type id] type))
                    
(defmulti write-session
  "Write a new or existing session to the session store."
  (fn [type session] type))

(defmulti destroy-session
  "Remove the session from the session store."
  (fn [type session] type))

(defmulti set-session-cookie
  "Add the session cookie to the given response."
  (fn [type response session] type))

;; Default implementations of create-session and set-session-cookie

(defmethod create-session :default
  [_]
  {:id (gen-uuid)})

(defmethod set-session-cookie :default
  [_ response session]
  (update-response {} response
    (set-cookie :session-id (session :id))))

;; In memory sessions

(def memory-sessions (ref {}))

(defmethod read-session :memory
  [_ id]
  (@memory-sessions id))

(defmethod write-session :memory
  [_ session]
  (dosync
    (alter memory-sessions
      assoc (session :id) session)))

(defmethod destroy-session :memory
  [_ session]
  (dosync
    (alter memory-sessions
      dissoc (session :id))))

;; Session middleware

(defn- get-request-session
  "Retrieve the session using the 'session-id' cookie in the request."
  [request]
  (if-let [session-id (-> request :cookies :session-id)]
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

(defn- assoc-request-flash
  "Associate the session flash with the request and remove it from the
  session."
  [request]
  (let [session (:session request)]
    (-> request 
      (assoc :flash   (session :flash {}))
      (assoc :session (dissoc session :flash)))))

(defn- get-response-session
  "Retrieve the current session from the response map."
  [request response]
  (assoc (:session response)
    :id (-> request :session :id)))

(defn with-session
  "Wrap a handler in a session."
  [handler]
  (fn [request]
    (let [request  (-> request
                     assoc-request-session
                     assoc-request-flash)
          response (handler request)
          session  (get-response-session request response)]
      ; Save session
      (if (:session response)
        (write-session *session-store* session)
        (if (not-empty (:flash request))
          (write-session *session-store* (:session request))))

      ; Set cookie
      (if (and response (:new-session? request))
        (set-session-cookie *session-store* response session)
        response))))

;; User functions for modifying the session

(defn set-session
  "Return a response map with the session set."
  [session]
  {:session session})

(defn alter-session
  "Use a function to alter the session."
  [func & args]
  (fn [request]
    (set-session
      (apply func (request :session) args))))

(defn session-assoc
  "Associate key value pairs with the session."
  [& keyvals]
  (apply alter-session assoc keyvals))

(defn session-dissoc
  "Dissociate keys from the session."
  [& keys]
  (apply alter-session dissoc keys))

(defn flash-assoc
  "Associate key value pairs with the session flash."
  [& keyvals]
  (alter-session merge {:flash (apply hash-map keyvals)}))
