(load-file "lib/compojure.clj")
(refer 'compojure)

; Load up all modules
(require-glob "modules/*/init.clj")

(def *server* (http-server *servlet*))

; Load up application
(require-glob "app/**/*.clj")
