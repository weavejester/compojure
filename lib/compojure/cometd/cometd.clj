;; An interface to Jetty's cometd implementation
(ns compojure.cometd
  (:import (org.mortbay.cometd.continuation ContinuationCometdServlet)
           (dojox.cometd MessageListener)))

(def *cometd*
  (new ContinuationCometdServlet))

;;;; Sending and receiving

(defn new-client
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

(defn publish
  "Publish a message to a channel."
  ([channel message]
    (publish (new-client) channel message))
  ([client channel message]
    (publish *cometd* client channel message))
  ([servlet client channel message]
    (.publish (get-channel servlet channel) client message nil)))

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

