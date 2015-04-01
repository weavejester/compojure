## 1.3.3 (2015-01-01)

* Updated Clout dependency to 2.1.1 to fix Clojure 1.7.0-alpha6 issue (see #137)

## 1.3.2 (2015-01-19)

* Fixed performance issue with routes in closures (see #133)
* Updated Medley dependency to 0.5.5

## 1.3.1 (2014-12-05)

* Don't initiate middleware passed to `wrap-routes` more than once

## 1.3.0 (2014-12-04)

* Fixed context macro when used with list and regex
* Updated Clout dependency to 2.1.0 for inline regex syntax in routes

## 1.2.2 (2014-11-29)

* Don't treat vector responses as handler functions
* Updated Ring dependency to 1.3.2

## 1.2.1 (2014-10-24)

* Fixed warning message in Clojure 1.7
* Fixed regression on index file content type in `route/files`

## 1.2.0 (2014-10-01)

* Minimum Clojure version is now 1.5.1
* Improved URL and File responses
* Added `rfn` macro
* Added `wrap-routes` function
* Made `compile-route` function public
* Updated Clout dependency to 2.0.0
* Fixed remaining context URL encoding bug
* Deprecated `compojure.handler` namespace in favor of Ring-Defaults
* Warning for using `*` as an route argument

## 1.1.9 (2014-09-07)

* `_method` parameter works for multipart forms
* Updated Ring dependency to 1.3.1

## 1.1.8 (2014-05-11)

* Fixed URL-encoding bug in routes defined within the context macro
* Removed core.incubator dependency
* Improved docstrings
* Updated Clout dependency to 1.2.0

## 1.1.7 (2014-05-07)

* Updated Ring dependency to 1.2.2

## 1.1.6 (2013-10-30)

* Updated Ring dependency to 1.2.1
* `_method` parameter made case insensitive

## 1.1.5 (2013-01-13)

* Updated Ring dependency to 1.1.7

## 1.1.4 (2013-01-11)

* compojure.response/render method retains metadata on responses

## 1.1.3 (2012-09-03)

* Updated Ring dependency to 1.1.5

## 1.1.2 (2012-09-02)

* Updated Ring dependency to 1.1.4

## 1.1.1 (2012-07-13)

* Updated Ring dependency to 1.1.1

## 1.1.0 (2012-05-24)

* Added PATCH route macro
* Added OPTION route macro
* Added low-level make-route function

## 1.0.4 (2012-05-06)

* Fixed context macro to allow lists for prefix

## 1.0.3 (2012-04-28)

* Updated Ring dependency to 1.1.0
* Fixed lost headers in compojure.route/files

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
