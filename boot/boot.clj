;; This file will be evaluated when script/repl or script/run are executed.
;; Populate this file with the code required to start your application.

;; An example boot setup is included below:
(use 'compojure.jetty)

;; Load app/example.clj
(load-file "app/example.clj")

;; Define a new HTTP server on port 8080, with the hello-world servlet
;; defined in app/example.clj
(defserver server
  {:port 8080}
  "/*" example/hello-world)

;; Start the server
(start server)
