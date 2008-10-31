;; compojure.cometd
;;
;; An interface to Jetty's cometd implementation.

(ns compojure.cometd
  (:use    (compojure control
                      jetty
                      str-utils)
           (clojure.contrib def
                            memoize))
  (:import (clojure.lang Sequential)
           (java.util Collection
                      HashMap
                      Map)
           (dojox.cometd Bayeux
                         Client
                         MessageListener
                         RemoveListener
                         SecurityPolicy)
           (org.mortbay.cometd.continuation ContinuationBayeux
                                            ContinuationCometdServlet)))

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

;;;; Servlet and rule chains ;;;;

(defvar *subscribe-rules*
  (ref (list))
  "Default rule chain for subscriptions.")

(defvar *publish-rules*
  (ref (list))
  "Default rule chain for publishing.")

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

(defn new-bayeux
  "Create a new Bayeux object with the given security rules."
  [subscribe-rules publish-rules]
  (doto (new ContinuationBayeux)
          (setSecurityPolicy
            (create-security-policy
              subscribe-rules
              publish-rules))))

(defvar *bayeux*
  (new-bayeux *subscribe-rules* *publish-rules*)
  "Default Bayeux object.")

(defn cometd-servlet
  "Create a new ContinuationCometdServlet in a ServletHolder wrapper, with the
  supplied Bayeux object and init parameters. Uses the default *bayeux* if the
  Bayeux object is not supplied."
  [& params]
  (let [[bayeux params] (if (instance? Bayeux (first params))
                          (split-at 1 params)
                          [*bayeux* params])]
    (apply servlet-holder
      (proxy [ContinuationCometdServlet] []
        (newBayeux [] bayeux))
      params)))

(defn- local-client?
  "Is the message being sent locally?"
  [client channel message]
  (if client
    (.isLocal client)))

(allow-subscribe local-client?)   ; Allow local access by default
(allow-publish local-client?)
(allow-publish "/meta/*")         ; Allow client to report meta events

;;;; Sending and receiving ;;;;

(defn singleton-client
  "Creates a new client the first time its called, and returns the same
  client for each subsequent call."
  [bayeux]
  (.newClient bayeux "publisher"))

(decorate-with memoize singleton-client)

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
    (publish (singleton-client *bayeux*) channel message))
  ([client channel message]
    (publish *bayeux* client channel message))
  ([bayeux client channel message]
    (.publish (.getChannel bayeux channel true)
              client
              (clj->java message)
              nil)))

(defn deliver
  "Deliver a message to a specific client."
  ([client channel message]
    (deliver client (singleton-client *bayeux*) channel message))
  ([client from channel message]
    (.deliver client from channel (clj->java message) nil)))

(defn- seqable?
  "Can x be turned into a Clojure sequence?"
  [x]
  (or (instance? Collection x)
      (and x (.isArray (class x)))))

(defn- java->clj
  "Turn a standard Java object into its equivalent Clojure data structure."
  [data]
  (cond
    (instance? Map data)
      (apply hash-map
        (mapcat
          #(list (keyword (java->clj (.getKey %)))
                 (java->clj (.getValue %)))
          data))
    (seqable? data)
      (vec (map java->clj data))
    otherwise
      data))

(defn subscribe
  "Subscribe to a channel."
  ([channel func]
    (subscribe *bayeux* channel func))
  ([bayeux channel func]
    (let [client (.newClient bayeux "subscriber")]
      (.addListener client
        (proxy [MessageListener] []
          (deliver [from to mesg]
            (func from (java->clj mesg)))))
      (.subscribe (.getChannel bayeux channel true) client)
      client)))

(defn unsubscribe
  "Unsubscribe from a channel."
  ([channel]
    (doseq client (.getSubscribers (.getChannel *bayeux* channel))
      (unsubscribe client channel)))
  ([client channel]
    (unsubscribe *bayeux* client channel))
  ([bayeux client channel]
    (.unsubscribe (.getChannel bayeux channel) client)))

(defn on-timeout
  "Calls the supplied function when the client times out."
  [#^Client client func]
    (.addListener client
      (proxy [RemoveListener] []
        (removed [id timeout?]
          (if timeout?
            (func client))))))
