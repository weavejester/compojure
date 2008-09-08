Compojure is an open source web framework for the Clojure programming language,
designed to be a fast, concise and functional way of developing applications.
It's only requirement is a woring Java VM, as it comes bundled with jar files
for Clojure and the Java HTTP server, Jetty.

Compojure is still in active development, but a lot of the API is now
relatively stable.

Quickstart
==========

1. Grab Compojure from github:

        $ git clone git://github.com/weavejester/compojure.git

2. Run Compojure:

        $ script/run

3. An example "Hello World" application should be up and running at:
http://localhost:8080/


File Structure
==============

By default, Compojure is organised with the file structure listed below. But
there's nothing stopping you taking the Compojure libraries and using them in
any fashion you want.

    +- app             - Your main application code
    |
    +- boot
    |  +- boot.clj     - The script that initializes your application
    |
    +- jars            - The jar files used by the application
    |
    +- lib
    |  +- compojure    - The Compojure libraries
    |
    +- public          - Static files that are served if no route is found
    |
    +- script
       +- repl         - Starts an interactive REPL
       +- run          - Runs non-interactively


Core Libraries
==============

Compojure provides several core libraries that provide the bulk of its
functionality.

HTTP
----

The HTTP module provides Compojure with a RESTful and functional way to define
Java servlets. It's syntax was heavily inspired by the Ruby web framework,
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


HTML
----

The HTML module provides a way of defining HTML or XML through a tree of
vectors.

    (html [:p [:em "Hello World"]])

    => <p>
         <em>Hello World</em>
       </p>

The tag is taken from the first item of the vector, and can be a string,
symbol or keyword. You can optionally specify attributes for the tag by
providing a hash map as the secord item of the vector:

    (html [:div {:class "footer"} "Page 1"])

    => <div class="footer">
         Page 1
       </div>

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
