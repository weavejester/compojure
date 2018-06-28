# Compojure

[![Build Status](https://travis-ci.org/weavejester/compojure.svg?branch=master)](https://travis-ci.org/weavejester/compojure)

Compojure is a small routing library for [Ring][1] that allows web
applications to be composed of small, independent parts.

## Installation

Add the following dependency to your `project.clj` file:

    [compojure "1.6.1"]

## Documentation

All documentation can be found on [cljdoc](https://cljdoc.xyz/d/compojure/compojure/CURRENT):

- [Getting Started](https://cljdoc.xyz/d/compojure/compojure/CURRENT/doc/getting-started)
- [Routes In Detail](https://cljdoc.xyz/d/compojure/compojure/CURRENT/doc/routes-in-detail)
- [Destructuring Syntax](https://cljdoc.xyz/d/compojure/compojure/CURRENT/doc/destructuring-syntax)
- [Nesting Routes](https://cljdoc.xyz/d/compojure/compojure/CURRENT/doc/nesting-routes)

As well as an [overview over Compojure's API](https://cljdoc.xyz/d/compojure/compojure/CURRENT/api/compojure)

## Community

* [Google Group](http://groups.google.com/group/compojure)
* #compojure on [Freenode](http://freenode.net/) IRC

## Usage

This small Compojure application demonstrates creating a Ring handler
from two routes:

```clojure
(ns hello-world.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]))

(defroutes app
  (GET "/" [] "<h1>Hello World</h1>")
  (route/not-found "<h1>Page not found</h1>"))
```

Also refer to the [Getting Started][2] documentation.

[1]:https://github.com/ring-clojure/ring
[2]:https://cljdoc.xyz/d/compojure/compojure/CURRENT/doc/getting-started

## License

Copyright © 2018 James Reeves

Distributed under the Eclipse Public License, the same as Clojure.
