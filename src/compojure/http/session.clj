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
  (:use compojure.http.response)
  (:use compojure.encodings)
  (:use compojure.crypto)
  (:use clojure.contrib.except))

;; Global session store type

(def *session-store* :memory)

(defn set-session-store!
  "Set the global session store type (defaults to :memory)."
  [store]
  (def *session-store* store))

;; Override these mulitmethods to create your own session storage

(defmulti create-session
  "Create a new session map. Should not attempt to save the session."
  (fn [type] type))
  
(defmulti read-session
  "Read in the session using the supplied data. Usually the data is a key used
  to find the session in a store."
  (fn [type data] type))
                    
(defmulti write-session
  "Write a new or existing session to the session store."
  (fn [type session] type))

(defmulti destroy-session
  "Remove the session from the session store."
  (fn [type session] type))

(defmulti session-cookie
  "Return the session data to be stored in the cookie. This is usually the
  session ID."
  (fn [type new? session] type))

;; Default implementations of create-session and set-session-cookie

(defmethod create-session :default
  [_]
  {:id (gen-uuid)})

(defmethod session-cookie :default
  [_ new? session]
  (if (not new?)
    (session :id)))

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

;; Cookie sessions

; Random secret key
(def *session-secret-key* (gen-uuid))

(defn- session-hmac
  "Calculate a HMAC for a marshalled session"
  [cookie-data]
  (hmac *session-secret-key* "HmacSHA256" cookie-data))

(defmethod create-session :cookie [_] {})

(defmethod session-cookie :cookie
  [_ _ session]
  (let [cookie-data (marshal session)]
    (if (> (count cookie-data) 4000)
      (throwf "Session data exceeds 4K")
      (str cookie-data "--" (session-hmac cookie-data)))))

(defmethod read-session :cookie
  [_ data]
  (let [[session mac] (.split data "--")]
    (if (= mac (session-hmac session))
      (unmarshal session))))

; Do nothing for write or destroy
(defmethod write-session :cookie [_ _])
(defmethod destroy-session :cookie [_ _])

;; Session middleware

(defn- get-request-session
  "Retrieve the session using the 'session-id' cookie in the request."
  [request]
  (if-let [session-id (-> request :cookies :session)]
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
  (if-let [id (-> request :session :id)]
    (assoc (:session response) :id id)
    (:session response)))

(defn- set-session-cookie
  "Set the session cookie on the response."
  [request response session]
  (let [new?   (:new-session? request)
        cookie (session-cookie *session-store* new? session)
        update (set-cookie :session cookie)]
    (update-response {} response update)))

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
      (if response
        (set-session-cookie request response session)
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
