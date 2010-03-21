Compojure is a small, open source web framework for the
[Clojure](http://clojure.org) programming language.

This is the latest development version of Compojure. For the latest stable
version, see [0.3.2](http://github.com/weavejester/compojure/tree/0.3.2).

An Example
----------

Here's a small web application written using Compojure,
[Ring](http://github.com/mmcgrana/ring) and
[Hiccup](http://github.com/weavejester/hiccup).

    (ns hello-world
      (:use [compojure.core :only (defroutes GET ANY)]
            [hiccup.core :only (html)]
            [ring.adapter.jetty :only (run-jetty)]
            [ring.util.response :only (redirect)]))

    (defroutes main-routes
      (GET "/" []
        (redirect "/world"))
      (GET "/:name" [name]
        (html [:h1 "Hello " name]))
      (ANY "*" {uri :uri}
        {:status 404
         :body (html [:h1 "Page not found: " uri])}))

    (run-jetty main-routes {:port 8080})


Installing
----------

The easiest way to use Compojure in your own projects is via
[Leiningen](http://github.com/technomancy/leiningen). Add the following
dependency to your project.clj file:

    [compojure "0.4.0-SNAPSHOT"]

To build Compojure from source, run the following commands:

    lein deps
    lein jar


Mailing List
------------

Compojure has a [Google Group](http://groups.google.com/group/compojure). This
is the best place to ask questions and report bugs.
