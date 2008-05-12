(in-ns 'compojure/plugins)
(clojure/refer 'clojure)
(clojure/refer 'compojure)

(import '(org.mortbay.jetty Server)
        '(org.mortbay.jetty.handler AbstractHandler)
        '(javax.servlet.http HttpServletRequest HttpServletResponse))

(def *server* (new Server 8080))

(. *server* (setHandler
  (proxy [AbstractHandler] []
    (handle [target request response dispatch]
      (let [servlet (resource-servlet (find-resource request))]
        (if servlet (servlet request response)))
      (. request (setHandled true))))))

(defmacro server [action]
  (cond
    (= action 'start) (. *server* (start))
    (= action 'stop)  (. *server* (stop))))
