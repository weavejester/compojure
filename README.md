Compojure is a small, open source web framework for the
[Clojure](http://clojure.org) programming language. 

An Example
----------

Here's a small web application written using Compojure,
[Ring](http://github.com/mmcgrana/ring) and
[Hiccup](http://github.com/weavejester/hiccup).

    (ns hello-world
      (:use [compojure.core :only (defroutes GET ANY)]
            [hiccup.core :only (html)]
            [ring.adapter.jetty :only (run-jetty)]
            [ring.util.response :only (redirect)])

    (defroutes main-routes
      (GET "/" []
        (redirect "/world"))
      (GET "/:name" [name]
        (html [:h1 "Hello " name]))
      (ANY "*" {uri :uri}
        {:status 404
         :body (html [:h1 "Page not found: " uri])}))

    (run-jetty main-routes {:port 8080})


Community
---------

The [Compojure Google Group](http://groups.google.com/group/compojure) is
the best place to ask questions about Compojure, suggest improvements or to 
reportbugs.
