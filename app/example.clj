;; This is a basic
(ns example
  (:use (compojure html http)))

;; A function to generate the standard outline of a HTML page.
(defn template
  [title & body]
  (html
    [:html
      [:head
        [:title title]]
      [:body
        body]]))

;; Define a HttpServlet called hello-world
(defservlet hello-world
  "Basic Hello-World servlet."
  (GET "/"
    (template "Hello World"
      [:h1 "Hello World"]))
  (ANY "/*"
    (page-not-found)))
