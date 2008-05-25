(load-file "lib/compojure.clj")
(refer 'compojure)

; Load up all plugins
(load-glob "plugins/*/main.clj")

(def *server* (new-server *servlet*))

; Load up application
(load-glob "app/**/*.clj")
