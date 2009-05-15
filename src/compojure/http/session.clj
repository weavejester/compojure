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
  (:use compojure.http.request)
  (:use compojure.http.response)
  (:use compojure.encodings)
  (:use compojure.crypto)
  (:use clojure.contrib.except))

;; Global session store type

(declare *session-repo*)

;; Override these mulitmethods to create your own session storage.
;; Uses the Compojure repository pattern.

(defn- repository-type
  [repository]
  (:type repository repository))

(defmulti create-session
  "Create a new session map. Should not attempt to save the session."
  (fn [] (repository-type *session-repo*)))

(defmulti read-session
  "Read in the session using the supplied data. Usually the data is a key used
  to find the session in a store."
  (fn [data] (repository-type *session-repo*)))

(defmulti write-session
  "Write a new or existing session to the session store."
  (fn [session] (repository-type *session-repo*)))

(defmulti destroy-session
  "Remove the session from the session store."
  (fn [session] (repository-type *session-repo*)))

(defmulti session-cookie
  "Return the session data to be stored in the cookie. This is usually the
  session ID."
  (fn [new? session] (repository-type *session-repo*)))

;; Default implementations of create-session and set-session-cookie

(defmethod create-session :default
  []
  {:id (gen-uuid)})

(defmethod session-cookie :default
  [new? session]
  (if new?
    (session :id)))

;; In memory sessions

(def memory-sessions (ref {}))

(defmethod read-session :memory
  [id]
  (@memory-sessions id))

(defmethod write-session :memory
  [session]
  (dosync
    (alter memory-sessions
      assoc (session :id) session)))

(defmethod destroy-session :memory
  [session]
  (dosync
    (alter memory-sessions
      dissoc (session :id))))

;; Cookie sessions

(def *default-secret-key* (gen-uuid))   ; Random secret key

(defn session-hmac
  "Calculate a HMAC for a marshalled session"
  [cookie-data]
  (let [secret-key (:secret-key *session-repo* *default-secret-key*)]
    (hmac secret-key "HmacSHA256" cookie-data)))

(defmethod create-session :cookie [] {})

(defmethod session-cookie :cookie
  [new? session]
  (let [cookie-data (marshal session)]
    (if (> (count cookie-data) 4000)
      (throwf "Session data exceeds 4K")
      (str cookie-data "--" (session-hmac cookie-data)))))

(defmethod read-session :cookie
  [data]
  (let [[session mac] (.split data "--")]
    (if (= mac (session-hmac session))
      (unmarshal session))))

; Do nothing for write or destroy
(defmethod write-session :cookie [session])
(defmethod destroy-session :cookie [session])

;; Session middleware

(defn- get-request-session
  "Retrieve the session using the 'session' cookie in the request."
  [request]
  (if-let [session-data (-> request :cookies :compojure-session)]
    (read-session session-data)))

(defn- assoc-request-session
  "Associate the session with the request."
  [request]
  (if-let [session (get-request-session request)]
    (assoc request
      :session session)
    (assoc request
      :session      (create-session)
      :new-session? true)))

(defn- assoc-request-flash
  "Associate the session flash with the request and remove it from the
  session."
  [request]
  (let [session (:session request)]
    (-> request
      (assoc :flash   (session :flash {}))
      (assoc :session (dissoc session :flash)))))

(defn- set-session-cookie
  "Set the session cookie on the response if required."
  [request response session]
  (let [new?    (:new-session? request)
        cookie  (session-cookie new? session)
        update  (set-cookie :compojure-session cookie, :path "/")]
    (if cookie
      (update-response request response update)
      response)))

(defn- save-handler-session
  "Save the session for a handler if required."
  [request response session]
  (cond (and (contains? response :session)
             (nil? (response :session)))
        (destroy-session session)
        (or (:session response)
            (:new-session? request)
            (not-empty (:flash request)))
        (write-session session)))

(defn with-session
  "Wrap a handler in a session of the specified type. Session type defaults to
  :memory if not supplied."
  ([handler]
    (with-session handler :memory))
  ([handler session-repo]
    (fn [request]
      (binding [*session-repo* session-repo]
        (let [request  (-> request assoc-cookies
                                   assoc-request-session
                                   assoc-request-flash)
              response (handler request)
              session  (or (:session response) (:session request))]
          (when response
            (save-handler-session request response session)
            (set-session-cookie   request response session)))))))

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
