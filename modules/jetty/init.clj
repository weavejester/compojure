(refer 'lib)
(require jetty/jetty)
(require http/init)
(refer 'jetty)

(def *server*
  (http-server *servlet*))

(defmacro server [action]
 `(cond
    (= '~action '~'start) (. *server* (start))
    (= '~action '~'stop)  (. *server* (stop))))
