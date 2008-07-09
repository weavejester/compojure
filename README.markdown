Compojure is a modular web framework for the Clojure programming language. It's
only requirement is a Java VM, as it comes with jar files for Clojure and the
HTTP server, Jetty.

Note that Compojure is still in active development and rather unfinished.

Quickstart
==========

1. Grab Compojure from github:

    $ git clone git://github.com/weavejester/compojure.git

2. Run the inbuilt REPL script. This automatically includes all the relevant
   jars and Compojure libraries. A bourne-compatible shell is required (at
   least until someone creates an equivalent batch script).

    $ script/repl

3. Add a basic "Hello World" HTTP resource:

    user=> (GET "/" (htmldoc :title "Example" (:h1 "Hello World")))

4. Start the inbuilt Jetty HTTP server:

    user=> (server start)

5. Your page should be up and running at: http://localhost:8080/

File Structure
==============

Like Ruby on Rails, Merb, and other similar frameworks, Compojure uses a fixed
directory layout.

    .
    |- app             - Your web application code
    |
    |- config
    |  `- boot.clj     - The script that sets up Compojure
    |
    |- jars            - The jar files used by the application
    |
    |- lib             - Global libraries for Compojure
    |
    |- modules         - Contains all the Compojure modules
    |  `- *
    |     |- init.clj  - Each module has an init.clj to initiate it
    |     `- jars      - And may also have a directory for jar files
    |
    |- public          - Static files that are served if no route is found
    |
    `- script
       `- repl         - The sh script that starts the Compojure REPL


By default, the boot.clj file will load in any .clj file in the app directory,
including in subdirectories.


Core Modules
============

Compojure comes with several core modules that provide the bulk of its
functionality.

HTTP
----

The HTTP module provides Compojure with a RESTful and functional way to define
HTTP resources. It's syntax was heavily inspired by the Ruby web framework,
Sinatra.

There are four macros for generating resources:

    GET  POST  PUT  DELETE

These correspond to the HTTP methods of the same name. They take the form:

    (method route & body)

    e.g. (GET "/greet" "Hello visitor!")

The route can be fixed, or embedded with parameters. These parameters can be
accessed via the 'route' map:

    (GET "/greet/:name"
      (str "Hello " (route :name)))

Along with 'route', there are several other bindings available by default in
all resource declarations:

  * method          - the HTTP method
  * full-path       - the full path of the request 
  * param [name]    - a HTTP parameter
  * header [name]   - a HTTP header
  * route [name]    - a named part of the request path
  * session         - a ref to a session-specific map
  * mime [filename] - guesses the mimetype of a filename
  * request         - the HttpServletRequest object
  * context         - the HttpServletContext object
  * response        - the HttServletResponse object

For example:

    (GET "/form"
      (str "<p>Your current name is: " (@session :name) "</p>"
           "<form>Change name: <input name='name' type='text'>"
           "<input type='submit' value='Save'></form>"))

    (POST "/form"
      (dosync
        (alter session assoc :name (param :name))
        (str "Your name was changed to " (@session :name))))


It is possible to modify the response through the response object, but this is
almost never necessary. Instead, Compojure takes a functional approach,
constructing the HTTP response from the return value of the resource.

In the previous examples, you can see how returning a string adds to the
response body. Other standard Clojure types modify the response in different
ways:

 * java.lang.String  - adds to the response body
 * java.lang.Number  - changes the status code
 * Clojure hash map  - alters the HTTP headers
 * Clojure seq       - lazily adds to the response body
 * java.io.File      - streams the file to the response body

These modifications can be chained together using a standard Clojure vector:

    (GET "/text"
      [{"Content-Type" "text/plain"}
       "This is plain text."
       "And some more text."])

    (GET "/bad"
      [404 "<h1>This page does not exist!</h1>"])

    (GET "/download"
      (file "public/compojure.tar.gz"))   ; 'file' is an alias to 'new java.io.File'
