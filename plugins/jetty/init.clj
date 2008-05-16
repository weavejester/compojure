(in-ns 'compojure/plugins)
(clojure/refer 'clojure)
(clojure/refer 'compojure)

(import '(org.mortbay.jetty Server)
        '(org.mortbay.jetty.servlet Context ServletHolder))

(def *server* (new Server 8080))

(let [context (new Context *server* "/" (. Context SESSIONS))
      holder  (new ServletHolder compojure-servlet)]
  (. context (addServlet holder "/*")))

(defmacro server [action]
  (cond
    (= action 'start) (. *server* (start))
    (= action 'stop)  (. *server* (stop))))
