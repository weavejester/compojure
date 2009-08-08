;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.http.session
  "Functions for creating and updating HTTP sessions."
  (:use compojure.str-utils)
  (:use compojure.http.helpers)
  (:use compojure.http.request)
  (:use compojure.http.response)
  (:use compojure.encodings)
  (:use compojure.crypto)
  (:use clojure.contrib.except))

;; Override these mulitmethods to create your own session storage.
;; Uses the Compojure repository pattern.

(defmulti create-session
  "Create a new session map. Should not attempt to save the session."
  (fn [repository] (:type repository)))

(defmulti read-session
  "Read in the session using the supplied data. Usually the data is a key used
  to find the session in a store."
  (fn [repository data] (:type repository)))

(defmulti write-session
  "Write a new or existing session to the session store."
  (fn [repository session] (:type repository)))

(defmulti destroy-session
  "Remove the session from the session store."
  (fn [repository session] (:type repository)))

(defmulti session-cookie
  "Return the session data to be stored in the cookie. This is usually the
  session ID."
  (fn [repository new? session] (:type repository)))

;; Default implementations of create-session and set-session-cookie

(defmethod create-session :default
  [repository]
  {:id (gen-uuid)})

(defmethod session-cookie :default
  [repository new? session]
  (if new?
    (session :id)))

;; In memory sessions

(def memory-sessions (ref {}))

(defmethod read-session :memory
  [repository id]
  (@memory-sessions id))

(defmethod write-session :memory
  [repository session]
  (dosync
    (alter memory-sessions
      assoc (session :id) session)))

(defmethod destroy-session :memory
  [repository session]
  (dosync
    (alter memory-sessions
      dissoc (session :id))))

;; Cookie sessions

(def default-session-key
  (delay (gen-secret-key)))

(defn- get-session-key
  "Get the session key from the repository or use the default key."
  [repository]
  (force (repository :session-key default-session-key)))

(defmethod create-session :cookie
  [repository]
  {})

(defmethod session-cookie :cookie
  [repository new? session]
  (let [session-key (get-session-key repository)
        cookie-data (seal session-key session)]
    (if (> (count cookie-data) 4000)
      (throwf "Session data exceeds 4K")
      cookie-data)))

(defmethod read-session :cookie
  [repository data]
  (unseal (get-session-key repository) data))

(defmethod write-session :cookie
  [repository session])

(defmethod destroy-session :cookie
  [repository session])

;; Session middleware

(defn timestamp-after
  "Return the current time plus seconds as milliseconds."
  [seconds]
  (+ (* seconds 1000) (System/currentTimeMillis)))

(defn assoc-expiry
  "Associate an :expires-at key with the session if the session repository
  contains the :expires key."
  [repository session]
  (if-let [expires (:expires repository)]
    (assoc session :expires-at (timestamp-after expires))
    session))

(defn session-expired?
  "True if this session's timestamp is in the past."
  [session]
  (if-let [expires-at (:expires-at session)]
    (< expires-at (System/currentTimeMillis))))

(defn- get-session
  "Retrieve the session using the 'session' cookie in the request."
  [repository request]
  (if-let [session-data (-> request :cookies :compojure-session)]
    (read-session repository session-data)))

(defn- assoc-new-session
  "Associate a new session with a request."
  [repository request]
  (assoc request
    :session (assoc-expiry repository (create-session repository))
    :new-session? true))

(defn assoc-session
  "Associate the session with the request."
  [request repository]
  (if-let [session (get-session repository request)]
    (if (session-expired? session)
      (do
        (destroy-session repository session)
        (assoc-new-session repository request))
      (assoc request :session
        (assoc-expiry repository session)))
    (assoc-new-session repository request)))

(defn assoc-flash
  "Associate the session flash with the request and remove it from the
  session."
  [request]
  (let [session (:session request)]
    (-> request
      (assoc :flash   (session :flash {}))
      (assoc :session (dissoc session :flash)))))

(defn set-session-cookie
  "Set the session cookie on the response if required."
  [repository request response session]
  (let [new?   (:new-session? request)
        cookie (session-cookie repository new? session)
        update (set-cookie :compojure-session cookie
                           :path (repository :path "/"))]
    (if cookie
      (update-response request response update)
      response)))

(defn save-handler-session
  "Save the session for a handler if required."
  [repository request response session]
  (when (and (contains? response :session)
             (nil? (response :session)))
    (destroy-session repository session))
  (when (or (:session response)
            (:new-session? request)
            (not-empty (:flash request))
            (contains? repository :expires))
    (write-session repository session)))

(defn- keyword->repository
  "If the argument is a keyword, expand it into a repository map."
  [repository]
  (if (keyword? repository)
    {:type repository}
    repository))

(defn with-session
  "Wrap a handler in a session of the specified type. Session type defaults to
  :memory if not supplied."
  ([handler]
    (with-session handler :memory))
  ([handler repository]
    (fn [request]
      (let [repo     (keyword->repository repository)
            request  (-> request (assoc-cookies)
                                 (assoc-session repo)
                                 (assoc-flash))
            response (handler request)
            session  (or (:session response) (:session request))]
        (when response
          (save-handler-session repo request response session)
          (set-session-cookie   repo request response session))))))

;; Useful functions for modifying the session

(defn set-session
  "Return a response map with the session set."
  [session]
  {:session session})

(defn clear-session
  "Set the session to nil."
  [session]
  (set-session nil))

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
