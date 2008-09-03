;; Jetty interface for Compojure
(ns compojure.jetty
  (:import (org.mortbay.jetty Server)
           (org.mortbay.jetty.servlet Context ServletHolder)
           (org.mortbay.util.ajax ContinuationSupport)))

;;;;; HTTP server ;;;;

(defn- create-server
  "Construct a Jetty Server instance."
  [options servlets]
  (let [port     (or (options :port) 8080)
        server   (new Server port)
        context  (new Context server "/" (. Context SESSIONS))
        servlets (partition 2 servlets)]
    (doseq [path servlet] servlets
      (.addServlet context (new ServletHolder servlet) path))
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
