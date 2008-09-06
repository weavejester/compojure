(use '(compojure jetty html http))

(defn template
  [title & body]
  (html
    [:html
      [:head
        [:title title]]
      [:body
        body]]))

(defservlet hello
  "Basic Hello-World servlet."
  (GET "/"
    (template "Hello World"
      [:h1 "Hello World"]))
  (ANY "/*"
    (page-not-found)))

(defserver server
  {:port 8080}
  "/*" hello)

(start server)
