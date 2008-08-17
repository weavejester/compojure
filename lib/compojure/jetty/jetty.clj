;; Jetty interface for Compojure
(init-ns 'compojure.jetty)

(import '(org.mortbay.jetty Server)
        '(org.mortbay.jetty.servlet Context ServletHolder)
        '(org.mortbay.util.ajax ContinuationSupport))

;;;;; HTTP server ;;;;

(defn http-server
  "Create a new Jetty HTTP server with the supplied servlet."
  [& options]
  (let [options (apply hash-map options)
        port    (or (options :port) 8080)
        server  (new Server port)
        context (new Context server "/" (. Context SESSIONS))]
    (doseq [path servlet] (options :servlets)
      (.addServlet context (new ServletHolder servlet) path))
    server))

(defn start "Start a HTTP server."
  [server]
  (.start server))

(defn stop  "Stop a HTTP server."
  [server]
  (.stop server))
