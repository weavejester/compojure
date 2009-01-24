;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.jetty:
;; 
;; Clojure interface to start an embedded Jetty server.

(ns compojure.server.jetty
  (:use compojure.control)
  (:import java.net.URL)
  (:import org.mortbay.jetty.Server)
  (:import org.mortbay.jetty.servlet.Context)
  (:import org.mortbay.jetty.servlet.ServletHolder)
  (:import org.mortbay.util.ajax.ContinuationSupport))

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
  "Get a Context instant for a server and hostname."
  ([server]
    (get-context server nil))
  ([server host]
    (let [context (Context. server "/" Context/SESSIONS)]
      (if host
        (doto context (.setVirtualHosts (into-array [host])))
        context))))

(decorate-with memoize get-context)

(defn get-host-and-path
  "Splits a path or URL into its hostname and path."
  [url-or-path]
  (if (re-find #"^\w+://" url-or-path)
    (let [url (URL. url-or-path)]
      [(.getHost url) (.getPath url)])
    [nil url-or-path]))

(defn add-servlet!
  "Add a servlet to a Jetty server. Servlets can be connected to a relative
  path or an absolute URL. When connected to a URL, the function will try and
  use the hostname to set up a virtual host. Wildcards for the domain and path
  are allowed."
  [server url-or-path servlet]
  (let [[host path] (get-host-and-path url-or-path)
        context     (get-context server host)
        holder      (if (instance? ServletHolder servlet)
                       servlet
                      (ServletHolder. servlet))]
    (.addServlet context holder path)))

(defn- create-server
  "Construct a Jetty Server instance."
  [options servlets]
  (let [port     (or (options :port) 8080)
        server   (Server. port)
        servlets (partition 2 servlets)]
    (doseq [[url-or-path servlet] servlets]
      (add-servlet! server url-or-path servlet))
    server))

(defn http-server
  "Create a new Jetty HTTP server with the supplied options and servlets."
  [options & servlets]
  (if (map? options)
    (create-server options servlets)
    (create-server {} (cons options servlets))))

(defmacro defserver
  "Shortcut for (def name (http-server args))"
  [name & args]
  `(def ~name (http-server ~@args)))

(defn start "Start a HTTP server."
  [server]
  (.start server))

(defn stop  "Stop a HTTP server."
  [server]
  (.stop server))
