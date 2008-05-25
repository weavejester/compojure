(refer 'compojure)
(import '(org.mortbay.jetty Server)
        '(org.mortbay.jetty.servlet Context ServletHolder))

(defn new-server
  [servlet & options]
  (let [options (apply hash-map options)
        port    (or (options :port) 8080)
        server  (new Server port)
        context (new Context server "/" (. Context SESSIONS))
        holder  (new ServletHolder servlet)]
    (. context (addServlet holder "/*"))
    server))

(def *server*)

(defmacro server [action]
  (cond
    (= action 'start) (. *server* (start))
    (= action 'stop)  (. *server* (stop))))
