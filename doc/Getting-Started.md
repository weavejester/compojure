# Getting Started

The simplest way to get started with Compojure is to use [Leiningen](http://github.com/technomancy/leiningen), the standard Clojure build tool. If you haven't already, download and install Leiningen. You'll need Leiningen 2.0.0 or later for these next instructions to work.

Next, create a new project using the "compojure" template:

```bash
lein new compojure hello-world
```

This will create a basic project skeleton containing a minimal web application.

You can now change into the project directory, and start a development server using Leiningen:

```bash
cd hello-world
lein ring server-headless
```

The dev server will start up on port 3000. If you make a change to one of the source files, it will automatically reload it for you.

You can also create a runnable jar file of your web application:

    lein ring uberjar

copy the resulting standalone jar to wherever you like, and run it in the usual way:

    java -jar path/to/hello-world-0.1.0-standalone.jar
