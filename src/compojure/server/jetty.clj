;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.server.jetty
  "Clojure interface to start an embedded Jetty server."
  (:use compojure.control
        compojure.server.common)
  (:import org.mortbay.jetty.Server
           [org.mortbay.jetty.servlet Context ServletHolder]
           org.mortbay.jetty.bio.SocketConnector
           org.mortbay.jetty.security.SslSocketConnector))

(defn servlet-holder
  "Wrap a servlet in a ServletHolder object with a supplied set of parameters
  to be set on servlet init."
  [servlet & params]
  (let [holder (new ServletHolder servlet)
        params (partition 2 params)]
    (doseq [[key val] params]
      (.setInitParameter holder (name key) (str val)))
    holder))

(defn get-context
  "Get a Context instance for a server and hostname."
  ([server]
    (get-context server nil))
  ([server host]
    (let [context (Context. server "/" Context/SESSIONS)]
      (if host
        (doto context (.setVirtualHosts (into-array [host])))
        context))))

(decorate-with memoize get-context)

(defn add-servlet!
  "Add a servlet to a Jetty server. Servlets can be connected to a relative
  path or an absolute URL. When connected to a URL, the function will try and
  use the hostname to set up a virtual host. Wildcards for the domain and path
  are allowed."
  [server url-or-path servlet]
  (prn (class servlet))
  (let [[host path] (get-host-and-path url-or-path)
        context     (get-context server host)
        holder      (if (instance? ServletHolder servlet)
                      servlet
                      (ServletHolder. servlet))]
    (.addServlet context holder path)))

(defn- add-ssl-connector!
  "Add an SslSocketConnector to a Jetty server."
  [server options]
  (let [ssl-connector (SslSocketConnector.)]
    (doto ssl-connector
      (.setPort        (options :ssl-port 443))
      (.setKeystore    (options :keystore))
      (.setKeyPassword (options :key-password)))
    (when (options :truststore)
      (.setTruststore ssl-connector (options :truststore)))
    (when (options :trust-password)
      (.setTrustPassword ssl-connector (options :trust-password)))
    (.addConnector server ssl-connector)))

(defn- create-server
  "Construct a Jetty Server instance."
  [options servlets]
  (let [connector (doto (SocketConnector.)
                    (.setPort (options :port 80))
                    (.setHost (options :host)))
        server    (doto (Server.)
                    (.addConnector connector))
        servlets  (partition 2 servlets)]
    (when (or (options :ssl) (options :ssl-port))
      (add-ssl-connector! server options))
    (doseq [[url-or-path servlet] servlets]
      (add-servlet! server url-or-path servlet))
    server))

(defn jetty-server
  "Create a new Jetty HTTP server with the supplied options and servlets."
  [options? & servlets]
  (server-with-options create-server options? servlets))

(defmacro defserver
  "Shortcut for (def name (http-server args))"
  [name & args]
  `(def ~name (jetty-server ~@args)))

(defn start "Start a HTTP server."
  [server]
  (.start server))

(defn stop  "Stop a HTTP server."
  [server]
  (.stop server))

(defn run-server
  "Create and start a new Jetty HTTP server."
  [& server-args]
  (let [server (apply jetty-server server-args)]
    (.start server)
    server))
