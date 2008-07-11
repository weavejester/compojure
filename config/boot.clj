(load-file "lib/lib.clj")
(lib/use compojure)
(lib/use glob)
(lib/use (html  :in "html"))
(lib/use (http  :in "http"))
(lib/use (jetty :in "jetty"))

(doseq file (glob "app/**.clj")
  (load-file (str file)))

(def server
  (http-server resource-servlet))

(. server (start))
