(load-file "modules/jetty/server.clj")
(refer 'jetty-server)

(def *server*)

(defmacro server [action]
  (cond
    (= action 'start) (. *server* (start))
    (= action 'stop)  (. *server* (stop))))
