(load-file "modules/jetty/server.clj")

(def *server*)

(defmacro server [action]
  (cond
    (= action 'start) (. *server* (start))
    (= action 'stop)  (. *server* (stop))))
