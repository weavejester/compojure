Compojure is an open source web framework for the Clojure programming language,
designed to produce concise, correct, functional code with the minimum of fuss.

Compojure is still in active development, but it is rapidly approaching version
0.1.

Building
========

1. Grab Compojure from github:

        $ git clone git://github.com/weavejester/compojure.git

2. Build Compojure:

        $ ant

3. Include build/compojure.jar in your classpath.


Dependencies
============

All of the following dependencies are stored in the 'deps' subdirectory for
convinience.

Clojure and Clojure-Contrib are required libraries for Compojure:

- clojure.jar

       svn co https://clojure.svn.sourceforge.net/svnroot/clojure clojure

- clojure-contrib.jar

       svn co https://clojure-contrib.svn.sourceforge.net/svnroot/clojure-contrib clojure-contrib

Jetty is used as the default web server for Compojure, but because Compojure
uses standard Java servlets, you could potentially use any web server with
servlet support.

- jetty-6.1.14.jar, jetty-util-6.1.14.jar and servlet-api-2.5-6.1.14.jar

       http://dist.codehaus.org/jetty/jetty-6.1.14/jetty-6.1.14.zip

For unit testing, Compojure relies on Fact:

- fact.clj

       git clone git://github.com/weavejester/fact.git


Core Libraries
==============

Compojure provides several core libraries that provide the bulk of its
functionality.

HTTP
----

The HTTP library provides Compojure with a RESTful and functional way to define
Java servlets. It's syntax was inspired by the Ruby web framework, Sinatra.

To create a servlet, you pass a series of HTTP resource definitions to the
`servlet` function:

    (def #^{:doc "A simple greeter"} greet
      (servlet
        (GET "/greet" "Hello visitor!")
        (ANY "/*"     (page-not-found))))

Compojure also provides a `defservlet` macro:

    (defservlet greet
      "A simple greeter"
      (GET "/greet" "Hello visitor!")
      (ANY "/*"     (page-not-found)))

The resource definitions passed to `defservlet` are Compojure's way of
associating a URL route with code that produces a useful HTTP response, such as
a web page or image.

Resource definitions take the form:

    (method route & body)

The method can be any one of the standard HTTP methods:

    GET  POST  PUT  DELETE  HEAD

Or, if you wish to match any HTTP method, you can use

    ANY

The route can be a fixed string, like "/greet", but often you're going to want
to assign certain parts of the route to parameters that affect the output:

    (GET "/greet/:name"
      (str "Hello " (route :name)))

Here, the resource definition assigns the path after "/greet" to the parameter
`:name`. Parameters from routes can be accessed via the `route` function.

Along with `route`, there are several other bindings available by default in
all resource declarations:

  * method          - the HTTP method
  * full-path       - the full path of the request 
  * params          - a hash-map of HTTP parameters
  * headers         - a hash-map of HTTP headers
  * cookies         - a hash-map of HTTP cookies
  * route           - a hash-map of named parts of the request path
  * session         - a ref to a session-specific map
  * (mime filename) - guesses the mimetype of a filename
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

 * java.lang.String         - adds to the response body
 * java.lang.Number         - changes the status code
 * Clojure map              - alters the HTTP headers
 * Clojure seq              - lazily adds to the response body
 * java.io.File             - streams the file to the response body
 * java.net.URL             - streams the resource of the URL to the response body
 * java.servlet.http.Cookie - adds the cookie to the HTTP headers

These modifications can be chained together using a standard Clojure vector:

    (GET "/text"
      [{"Content-Type" "text/plain"}
       "This is plain text."
       "And some more text."])

    (GET "/bad"
      [404 "<h1>This page does not exist!</h1>"])

    (GET "/download"
      (file "public/compojure.tar.gz"))   ; 'file' is an alias to 'new java.io.File'

Jetty
-----

The Jetty library provides a Clojure-friendly interface to the Jetty web
server, so that you can easily create a web server with servlet mappings.

    (def my-server
      (http-server {:port 8080}
        "/*"       my-main-servlet
        "/other/*" another-servlet
        ...))

You can also use the defserver macro:

    (defserver my-server
      {:port 8080}
      "/*"       my-main-servlet
      "/other/*" another-servlet
      ...))

Once you've created your Jetty server, use `(start my-server)` and
`(stop my-server)` to start and stop the web server.

HTML
----

The HTML library provides a way of defining HTML or XML through a tree of
vectors.

    (html [:p [:em "Hello World"]])

    => <p>
         <em>Hello World</em>
       </p>

The tag name is taken from the first item of the vector, and can be a string,
symbol or keyword. You can optionally specify attributes for the tag by
providing a hash map as the secord item of the vector:

    (html [:div {:class "footer"} "Page 1"])

    => <div class="footer">
         Page 1
       </div>

The html function additionally offers syntax sugar for defining id and class
attributes:

    (html [:h1.first  "Foo"]
          [:h2#second "Bar"])

    => <h1 class="first">Foo</h1>
       <h2 id="second">Bar</h2>

Any sequences will be expanded out into the containing vector:

    (html [:em '("foo" "bar")])

    => <em>foobar</em>

    (html [:ul
      (map (fn [x] [:li x])
           [1 2 3])])

    => <ul>
         <li>1</li>
         <li>2</li>
         <li>3</li>
       </ul>

The `html` function not only renders valid HTML, it also formats it as best it
can in a human readable format. Block elements like `<p>` and `<div>` are
indented, whilst span elements like `<em>` are rendered inline.

Conversely, the `xml` function has no special formatting.
