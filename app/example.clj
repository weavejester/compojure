(use '(compojure jetty html http))

(defn template
  [title & body]
  (html
    [:html
      [:head
        [:title title]]
      [:body
        body]]))

(resource hello
  (GET "/"
    (template "Hello World"
      [:h1 "Hello World"]))
  (ANY "/*"
    (page-not-found)))

(defserver server
  {:port 8080}
  "/*" (servlet hello))

(start server)
