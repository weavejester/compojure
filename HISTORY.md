## 1.0.2 (2012-04-06)

* Updated Ring dependency to 1.0.2
* not-found route accepts response maps
* Fixed context macro to allow vars for prefix

## 1.0.1 (2012-01-08)

* Updated Clout dependency to 1.0.1

## 1.0.0 (2011-12-25)

* Updated Ring dependency to 1.0.1
* Updated Clout dependency to 1.0.0
* Added let-routes macro
* Added wrap-flash middleware to compojure.handler/site
* Removed deprecated wrap! function
* Added content-type header for rendering seqs

## 0.6.5 (2011-07-17)

* Added multipart options to compojure.handler/site
* Added default character encoding of UTF-8
* Updated Ring to 0.3.11

## 0.6.4 (2011-06-28)

* Updated Ring dependency to 0.3.10

## 0.6.3 (2011-05-04)

* Removed dependency on clojure-contrib
* Updated Ring dependency to 0.3.8

## 0.6.2 (2011-03-13)

* GET routes now handle HEAD requests as well
* Resource routes now check classpath then servlet context
* Fixed possible issue with large resource streams

## 0.6.1 (2011-03-05)

* Route for files and resources guess content-type
* Updated Clout dependency to 0.4.1

## 0.6.0 (2011-02-13)

* Updated Ring dependency to 0.3.5
* Updated Clout dependency to 0.4.0
* Added context macro
* Added routing function
* Removed default middleware from routes and defroutes
* Added compojure.handler namespace
* Deprecated wrap! macro

## 0.5.3 (2010-11-16)

* Updated Ring dependency to 0.3.4

## 0.5.2 (2010-10-03)

* Updated Ring dependency to 0.3.1
* Updated Clout dependency to 0.3.1

## 0.5.1 (2010-09-28)

* Fixed intermittent render map bug

## 0.5.0 (2010-09-24)

* Updated Ring dependency to 0.3.0

## 0.4.1 (2010-07-13)

* Fixed '& more' destructuring bug
* Updated Ring dependency to 0.2.5

## 0.4.0 (2010-06-23)

* Factored middleware out to Ring 0.2
* Factored HTML generation out to Hiccup
* Factored route string parsing to Clout
