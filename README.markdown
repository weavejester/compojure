Compojure is an open source web framework for the [Clojure](http://clojure.org)
programming language. It emphasizes a thin I/O layer and a functional approach
to web development.

Compojure is still in active development. Version 0.1 is now available.

Sample Code
-----------

Here's a small web application written in Compojure:

    (use 'compojure)

    (defservlet my-servlet
      (GET "/"
        (html [:h1 "Hello World"]))
      (ANY "*"
        (page-not-found)))

    (run-server {:port 8080}
      "/*" my-servlet)

Dependencies
------------

To run Compojure, you'll need:

* The [Clojure](http://clojure.org) programming language
* The [Clojure-Contrib](http://code.google.com/p/clojure-contrib/) library
* A Java servlet container like [Jetty](http://www.mortbay.org/jetty/)

And to run Compojure's unit tests, you'll need:

* [Fact](http://github.com/weavejester/fact)
* [Re-rand](http://github.com/weavejester/re-rand)

Documentation
-------------

For information on how to get started and use Compojure, please see our
[Wiki](http://en.wikibooks.org/wiki/Compojure).

Community
---------

The [Compojure Group](http://groups.google.com/group/compojure) is the best place
to ask questions about Compojure, suggest improvements or to report bugs.

Tutorials
---------

Eric Lavigne has written a series of excellent tutorials on Compojure:

* [Install Compojure on a Slicehost VPS](http://ericlavigne.wordpress.com/2008/12/18/compojure-on-a-slicehost-vps/)
* [Using PostgreSQL with Compojure](http://ericlavigne.wordpress.com/2008/12/28/using-postgresql-with-compojure/)
* [Compojure security: authentication and authorization](http://ericlavigne.wordpress.com/2009/01/04/compojure-security-authentication-and-authorization/)
