(.loadResourceScript clojure.lang.RT "clojure/contrib/lib/lib.clj") 
(refer 'clojure.contrib.lib)

; Load all the Compojure libraries
(use '(compojure cometd html http jetty json persist))

; Create a new Jetty HTTP Server on port 8080
(def *server*
  (http-server
    :port     8080
    :servlets { "/cometd/*" cometd-servlet
                "/*"        resource-servlet }))

(ANY "/*" (not-found))              ; Add in default 404 route
(GET "/*" (serve-file (route :*)))  ; Add in a static file handler

; Start the server
(start *server*)
