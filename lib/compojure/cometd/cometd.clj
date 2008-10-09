;; compojure.cometd
;;
;; An interface to Jetty's cometd implementation

(ns compojure.cometd
  (:use    (compojure control
                      str-utils)
           (clojure.contrib def))
  (:import (clojure.lang Sequential)
           (java.util Collection
                      HashMap
                      Map)
           (dojox.cometd MessageListener
                         SecurityPolicy)
           (org.mortbay.cometd.continuation ContinuationCometdServlet)))

;;;; Security ;;;;

(defn- rules-allow?
  "Is the client allowed to perform this action?"
  [rules & args]
  (loop [rules rules]
    (if (seq rules)
      (let [[type pred] (first rules)]
        (if (apply pred args)
          type
          (recur (rest rules))))
      false)))

(defn create-security-policy
  "Create a extensible security policy for a cometd implementation."
  [subscribe-rules publish-rules]
  (proxy [SecurityPolicy] []
    (canHandshake [message]
      true)
    (canCreate [client channel message]
      (and client (not (.startsWith channel "/meta/"))))
    (canSubscribe [client channel message]
      (and (not (.startsWith channel "/meta/")
           (not= channel "/*")
           (not= channel "/**")
           (rules-allow? @subscribe-rules client channel message))))
    (canPublish [client channel message]
      (or (= channel "/meta/handshake")
          (rules-allow? @publish-rules client channel message)))))

(defn- match-channel
  "Match a channel name to a string with wildcards."
  [channel string]
  (.matches channel (.replace (re-escape string) "\\*" ".*")))

(defn add-rule-to-chain
  "Add a allow or deny predicate to an existing rule chain ref. If the a
  string is supplied in place of the predicate, the rule applies to the
  channel matching the string."
  [rules allow? pred]
  (if (string? pred)
    (add-rule-to-chain rules allow?
      (fn [client channel message]
        (match-channel channel pred)))
    (dosync
      (commute rules conj [allow? pred]))))

(defn create-cometd-servlet
  "Create a new ContinuationCometdServlet with the supplied security rule
  chain refs."
  [subscribe-rules publish-rules]
  (proxy [ContinuationCometdServlet] []
    (newBayeux []
      (doto (proxy-super newBayeux)
            (setSecurityPolicy
              (create-security-policy
                subscribe-rules
                publish-rules))))))

;;;; Default servlet and rule chains ;;;;

(defvar *subscribe-rules*
  (ref (list))
  "Default rule chain for subscriptions.")

(defvar *publish-rules*
  (ref (list))
  "Default rule chain for publishing.")

(defvar *cometd*
  (create-cometd-servlet
    *subscribe-rules*
    *publish-rules*)
  "Default cometd servlet object tied to default security rule chains.")

(defn allow-publish
  "Allow publishing of messages based on a predicate or channel name."
  [pred-or-channel]
  (add-rule-to-chain *publish-rules* true pred-or-channel))

(defn deny-publish
  "Deny publishing of messages based on a predicate or channel name."
  [pred-or-channel]
  (add-rule-to-chain *publish-rules* false pred-or-channel))

(defn allow-subscribe
  "Allow subscribing to a channel based on a predicate or channel name."
  [pred-or-channel]
  (add-rule-to-chain *subscribe-rules* true pred-or-channel))

(defn deny-subscribe
  "Deny subscribing to a channel based on a predicate or channel name."
  [pred-or-channel]
  (add-rule-to-chain *subscribe-rules* false pred-or-channel))

(defn- local-client?
  "Is the message being sent locally?"
  [client channel message]
  (if client
    (.isLocal client)))

(allow-subscribe local-client?)   ; Allow local access by default
(allow-publish local-client?)
(allow-publish "/meta/*")         ; Allow client to report meta events

;;;; Sending and receiving ;;;;

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

(defn deliver
  "Deliver a message to a specific client."
  ([client channel message]
    (deliver client (new-client) channel message))
  ([client from channel message]
    (.deliver client from channel (clj->java message) nil)))

(defn- java->clj
  "Turn a standard Java object into its equivalent Clojure data structure."
  [data]
  (cond
    (or (instance? Collection data) (.isArray (class data)))
      (vec (map java->clj data))
    (instance? Map data)
      (apply hash-map
        (mapcat
          #(list (keyword (java->clj (.getKey %)))
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
            (func from (java->clj mesg)))))
      (.subscribe (get-channel servlet channel) client)
      client)))

(defn unsubscribe
  "Unsubscribe from a channel."
  ([channel]
    (doseq client (.getSubscribers (get-channel *cometd* channel))
      (unsubscribe client channel)))
  ([client channel]
    (unsubscribe *cometd* client channel))
  ([servlet client channel]
    (.unsubscribe (get-channel servlet channel) client))) 
