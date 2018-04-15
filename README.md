# Compojure

[![Build Status](https://travis-ci.org/weavejester/compojure.svg?branch=master)](https://travis-ci.org/weavejester/compojure)

Compojure is a small routing library for [Ring][1] that allows web
applications to be composed of small, independent parts.

## Installation

Add the following dependency to your `project.clj` file:

    [compojure "1.6.1"]

## Documentation

* [Wiki](https://github.com/weavejester/compojure/wiki)
* [API Docs](http://weavejester.github.com/compojure)

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

Also refer to the [Getting Started][2] page on the wiki.

[1]:https://github.com/ring-clojure/ring
[2]:https://github.com/weavejester/compojure/wiki/Getting-Started

## License

Copyright © 2018 James Reeves

Distributed under the Eclipse Public License, the same as Clojure.
