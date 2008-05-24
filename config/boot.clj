(load-file "lib/compojure.clj")
(refer 'compojure)

(def #^{:doc "The root Compojure servlet"} *servlet*)

; Load up all plugins
(load-glob "plugins/*/init.clj")

; Load up application
(load-glob "app/**/*.clj")
