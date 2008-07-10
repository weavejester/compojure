(load-file "lib/lib.clj")
(lib/use compojure)
(lib/use (html  :in "html"))
(lib/use (http  :in "http"))
(lib/use (jetty :in "jetty"))

(def server
  (http-server resource-servlet))
