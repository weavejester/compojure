(load-file "lib/clojure/contrib/lib/lib.clj")
(refer 'clojure.contrib.lib)

; Load all the Compojure libraries
(use '(compojure html http jetty json persist))

; Create a new Jetty HTTP Server on port 8080
(def *server*
  (http-server resource-servlet :port 8080))

; Add in a static file handler
(GET "/*"
  (serve-file (route :*)))

; Start the server
(start *server*)
