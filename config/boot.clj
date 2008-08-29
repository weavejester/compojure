(.loadResourceScript clojure.lang.RT "clojure/contrib/lib/lib.clj") 
(refer 'clojure.contrib.lib)

; Load Compojure libraries
(use '(compojure html
                 http
                 jetty))

; Create a default servlet
(defservlet hello-world
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
    :servlets ["/*" hello-world]))

; Start the server
(start server)
