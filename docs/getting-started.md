---
layout: doc
title: Getting Started
---

The simplest way to get started with Compojure is to use [Leiningen][1].
Installing Leiningen is straightforward, and instructions to do so can be
found on the [Clojure Assembla Wiki][2].

Once Leiningen is installed, use it to create a new Clojure project:

{% highlight sh %}
lein new hello-www
cd hello-www
{% endhighlight %}

This will create a basic project skeleton we can build upon. Update
`project.clj` in the current directory to include Compojure 0.4.0 and the
Ring Jetty adapter as dependencies:

{% highlight clj %}
(defproject hello-www "1.0.0-SNAPSHOT"
  :description "A Compojure 'Hello World' application"
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [compojure "0.4.0-RC2"]
                 [ring/ring-jetty-adapter "0.2.0"]])
{% endhighlight %}

Next, use Leiningen to download the project dependencies for you:

{% highlight sh %}
lein deps
{% endhighlight %}

Now you're ready to write the application. Put the following code into
`src/hello-www/core.clj`:

{% highlight clj %}
(ns hello-www.core
  (:use compojure.core
        ring.adapter.jetty)
  (:require [compojure.route :as route]))

(defroutes example
  (GET "/" [] "<h1>Hello World Wide Web!</h1>")
  (route/not-found "Page not found"))

(run-jetty example {:port 8080})
{% endhighlight %}

To run this application, use:

{% highlight sh %}
lein repl src/hello-www/core.clj
{% endhighlight %}

Visit <http://localhost:8080> to see the results. If all has gone well, you'll
see your "Hello World" page.

[1]:http://github.com/technomancy/leiningen
[2]:http://www.assembla.com/wiki/show/clojure/Getting_Started_with_Leiningen
