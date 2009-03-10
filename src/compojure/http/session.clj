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

(def memory-sessions {})

(defn get-session-id
  "Get the session ID from the request."
  [request]
  (or (get-in request [:cookies "session-id"])
      (uuid)))

(defn set-session-id
  "Create a response with a session ID."
  [response session-id]
  (merge response
    (response-from
      (set-cookie "session-id" session-id))))

(defn with-session
  "Wrap a handler in a session."
  [handler]
  (fn [request]
    (let [session-id (get-session-id request)
          request    (assoc request :session-id session-id)
          response   (handler request)]
      (set-session-id response session-id))))
