# Compojure [![Build Status](https://github.com/weavejester/compojure/actions/workflows/test.yml/badge.svg)](https://github.com/weavejester/compojure/actions/workflows/test.yml)

Compojure is a small routing library for [Ring][1] that allows web
applications to be composed of small, independent parts.

## Installation

Add the following dependency to your `project.clj` file:

    [compojure "1.7.1"]

## Documentation

* [Wiki](https://github.com/weavejester/compojure/wiki)
* [API Docs](http://weavejester.github.io/compojure)

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

Copyright © 2024 James Reeves

Distributed under the Eclipse Public License, the same as Clojure.
