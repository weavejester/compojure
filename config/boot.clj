(load-file "lib/compojure.clj")
(refer 'compojure)

; Load up all modules
(load-glob "modules/*/init.clj")

(def *server* (new-server *servlet*))

; Load up application
(load-glob "app/**/*.clj")
