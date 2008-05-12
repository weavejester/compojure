(load-file "vendor/compojure.clj")
(refer 'compojure)

; Load up all plugins
(load-file-pattern
  (re-pattern "\\./plugins/[^/]+/init.clj"))

; Load up application
(load-file-pattern
  (re-pattern "\\./app/.*\\.clj"))

(refer 'compojure/plugins)
