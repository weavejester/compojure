;; An interface to Jetty's cometd implementation
(ns compojure.cometd)

(import '(org.mortbay.cometd.continuation ContinuationCometdServlet))

(def cometd-servlet
  (new ContinuationCometdServlet))

(defn- get-channel
  "Gets a cometd channel that we can publish or subscribe to."
  [channel]
  (.. cometd-servlet
      (getBayeux)
      (getChannel channel true)))
