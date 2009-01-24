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

(ns compojure.jetty
  (:import org.mortbay.jetty Server)
  (:import org.mortbay.jetty.servlet.Context)
  (:import org.mortbay.jetty.servlet.ServletHolder)
  (:import org.mortbay.util.ajax.ContinuationSupport))

;;;;; HTTP server ;;;;

(defn servlet-holder
  "Wrap a servlet in a ServletHolder object with a supplied set of parameters
  to be set on servlet init."
  [servlet & params]
  (let [holder (new ServletHolder servlet)
        params (partition 2 params)]
    (doseq [[key val] params]
      (.setInitParameter holder (name key) (str val)))
    holder))

(defn- create-server
  "Construct a Jetty Server instance."
  [options servlets]
  (let [port     (or (options :port) 8080)
        server   (new Server port)
        context  (new Context server "/" (. Context SESSIONS))
        servlets (partition 2 servlets)]
    (doseq [[path servlet] servlets]
      (.addServlet context
                   (if (instance? ServletHolder servlet)
                     servlet
                     (new ServletHolder servlet))
                   path))
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
