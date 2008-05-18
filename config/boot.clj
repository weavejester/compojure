(load-file "vendor/compojure.clj")
(refer 'compojure)

; Load up all plugins
(load-glob "plugins/*/init.clj")

; Load up application
(load-glob "app/**/*.clj")

(refer 'compojure/plugins)
