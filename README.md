Compojure is a small, open source web framework for the
[Clojure](http://clojure.org) programming language.

An Example
----------

Here's a small web application written using Compojure and
[Ring](http://github.com/mmcgrana/ring).

    (ns hello-world
      (:use compojure.core, ring.adapter.jetty)
      (:require [compojure.route :as route]))

    (defroutes main-routes
      (GET "/" [] "<h1>Hello World</h1>")
      (route/not-found "<h1>Page not found</h1>"))

    (run-jetty main-routes {:port 8080})

Documentation
-------------

* [Wiki](https://github.com/weavejester/compojure/wiki)
* [API Docs](http://weavejester.github.com/compojure)

Breaking Changes
----------------

As of version 0.6.0, Compojure no longer adds default middleware to
routes. This means you must explicitly add the `wrap-params` and
`wrap-cookies` middleware to your routes.

To make this a little easier, the [compojure.handler][1] namespace
provides functions that add common middleware functions to your routes. 

[1]: http://weavejester.github.com/compojure/compojure.handler-api.html

Installing
----------

The easiest way to use Compojure in your own projects is via
[Leiningen](http://github.com/technomancy/leiningen). Add the following
dependency to your project.clj file:

    [compojure "0.6.0"]

To build Compojure from source, run the following commands:

    lein deps
    lein jar

Mailing List
------------

Compojure has a [Google Group](http://groups.google.com/group/compojure). This
is the best place to ask questions and report bugs.
