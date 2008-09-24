;; compojure.cometd
;;
;; An interface to Jetty's cometd implementation

(ns compojure.cometd
  (:use    (compojure control))
  (:import (clojure.lang Sequential)
           (java.util Collection
                      HashMap
                      Map)
           (dojox.cometd MessageListener)
           (org.mortbay.cometd.continuation ContinuationCometdServlet)))

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

(defn- clj->java
  "Convert a clojure data structure into Java-compatible types."
  [data]
  (cond
    (keyword? data)
      (name data)
    (sequential? data)
      (map clj->java data)
    (map? data)
      (let [hmap (new HashMap)]
        (doseq [key val] data
          (.put hmap (clj->java key) (clj->java val)))
        hmap)
    otherwise
      data))

(defn publish
  "Publish a message to a channel."
  ([channel message]
    (publish (new-client) channel message))
  ([client channel message]
    (publish *cometd* client channel message))
  ([servlet client channel message]
    (.publish (get-channel servlet channel)
              client
              (clj->java message)
              nil)))

(defn- java->clj
  "Turn a standard Java object into its equivalent Clojure data structure."
  [data]
  (cond
    (instance? Collection data)
      (vec (map java->clj data))
    (instance? Map data)
      (apply hash-map
        (mapcat
          #(list (java->clj (.getKey %))
                 (java->clj (.getValue %)))
          data))
    otherwise
      data))

(defn subscribe
  "Subscribe to a channel."
  ([channel func]
    (subscribe *cometd* channel func))
  ([servlet channel func]
    (let [client (new-client)]
      (.addListener client
        (proxy [MessageListener] []
          (deliver [from to mesg]
            (func (java->clj mesg)))))
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
