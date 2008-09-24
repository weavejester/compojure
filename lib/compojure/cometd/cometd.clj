;; compojure.cometd
;;
;; An interface to Jetty's cometd implementation

(ns compojure.cometd
  (:use    (compojure json))
  (:import (dojox.cometd MessageListener)
           (org.mortbay.cometd.continuation ContinuationCometdServlet)
           (org.mortbay.util.ajax JSON$Generator)))

(def *cometd*
  (new ContinuationCometdServlet))

;;;; Sending and receiving

(defn new-client
  "Create a new cometd client object."
  ([]   (new-client "compojure"))
  ([id] (new-client *cometd* id))
  ([servlet id]
    (.. servlet (getBayeux)
                (newClient id))))

(defn- get-channel
  "Gets a cometd channel that we can publish or subscribe to."
  [servlet channel]
  (.. servlet (getBayeux)
              (getChannel channel true)))

(defn- json-generator
  "Creates a JSON.Generator object for outputting Clojure data structures."
  [data]
  (proxy [JSON$Generator] []
    (addJSON [buffer]
      (.append buffer (json data)))))

(defn publish
  "Publish a message to a channel."
  ([channel message]
    (publish (new-client) channel message))
  ([client channel message]
    (publish *cometd* client channel message))
  ([servlet client channel message]
    (.publish (get-channel servlet channel)
              client
              (json-generator message)
              nil)))

(defn subscribe
  "Subscribe to a channel."
  ([channel func]
    (subscribe *cometd* channel func))
  ([servlet channel func]
    (let [client (new-client)]
      (.addListener client
        (proxy [MessageListener] []
          (deliver [from to mesg]
            (func mesg))))
      (.subscribe (get-channel servlet channel) client))))

;;;; Security ;;;;

(comment
(defn allowed-to?)

(defn create-security-policy
  "Create a extensible security policy for an existing cometd servlet."
  [servlet]
  (.. servlet
      (getBayeux)
      (setSecurityPolicy
        (proxy SecurityPolicy
          (canPublish [])))))

(defn deny-subscribe
  "Deny subscription requests matching a predicate."
  [pred])
  

(defn deny-publish
  [pred])

)
