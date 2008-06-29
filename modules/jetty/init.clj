(load-file "modules/jetty/server.clj")
(refer 'jetty)

(defmacro server [action]
 `(cond
    (= ~action 'start) (. *server* (start))
    (= ~action 'stop)  (. *server* (stop))))
