Compojure is an open source web framework for the [Clojure](http://clojure.org)
programming language. It emphasizes a thin I/O layer and a functional approach
to web development.

Compojure is still in active development, but it is rapidly approaching version
0.1.

Sample Code
-----------

Here's a small sample servlet definition in Compojure:

    (defservlet demo
      (GET "/"
        (html [:h1 "Hello World"]))
      (ANY "*"
        (page-not-found)))

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
