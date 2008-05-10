(load-file "vendor/compojure.clj")
(refer 'compojure)
(load-file-pattern
  (re-pattern "\\./plugins/[^/]+/init.clj"))
