; Load Compojure libraries
(use '(compojure html
                 http
                 jetty))

; Create a default servlet
(resource hello
  (GET "/"
    (html
      (doctype :html4)
      [:html
        [:head
          [:title "Hello World"]]
        [:body
          [:h1 "Hello World"]]]))
  (GET "/*"
    (serve-file (route :*)))  ; Add in a static file handler
  (ANY "/*"
    (not-found)))             ; Add in default 404 route

; Create a new Jetty HTTP Server on port 8080
(def server
  (http-server
    :port     8080
    :servlets ["/*" (servlet hello)]))

; Start the server
(start server)
