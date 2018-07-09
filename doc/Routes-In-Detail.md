# Routes In Detail

Routes in Compojure look something like this:

```clojure
(GET "/user/:id" [id]
  (str "<h1>Hello user " id "</h1>"))
```

Routes return [Ring handler](https://github.com/ring-clojure/ring/wiki/Concepts#handlers) functions. Despite their syntax, there’s nothing magical about them. They just provide a concise way of defining functions to handle HTTP requests.


### Matching the HTTP method

Let’s unravel the syntax. The first symbol is:

`GET`

This is one of several route macros Compojure provides. This macro tests the HTTP request method, and if the method is not "GET", the function returns nil.

Other route macros you can use are `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH` and `HEAD`. If you want to match any HTTP method, use the `ANY` macro.

### Matching the URI

The first argument to the macro is the URI of the route:

```clojure
"/user/:id"
```

This is a string that uses the routing syntax defined by [Clout](https://github.com/weavejester/clout). It has a lot in common with the routing syntax used in Ruby on Rails and Sinatra.

It matches against the URI of the request. The `:id` part will match any sub-path up to the next "/" or ".", and puts the results in the "id" parameter.

If we wanted to be more specific, we could also define a custom regular expression for this parameter:

```clojure
["/user/:id", :id #"[0-9]+"]
```

Or use inline regular expressions surrounded by braces (introduced in Compojure 1.3.0):

```clojure
"/user/:id{[0-9]+}"
```

Like the HTTP method, if the URI does not match the defined path, the route function will return nil.

### Destructuring the request

After the HTTP method and the URI have been matched the second argument to the macro provides a way of retrieving information from the request map. This can either be a vector of parameters you want, or a full Clojure destructuring form.

```clojure
[id]
```

In other words, the above syntax binds the symbol `id` to the “id” parameter in the request map, which in this case was populated by the [Clout](https://github.com/weavejester/clout) route string (the `:id` in the URI from the previous section). We could also use a standard Clojure destructuring form:

```clojure
{{id :id} :params}
```

This provides more control, but is less succinct than the vector syntax.

Note that you can always capture the request map like so:

```clojure
(GET "/" request (str request))
```

This will return your request map as string as response.

See [[Destructuring Syntax part|Destructuring-Syntax]] for detailed description.

### Returning a response

Once the HTTP request has been matched and destructured, the rest of the route is encased in an implicit do block, so that it behaves like normal functions:

```clojure
(str "<h1>Hello user " id "</h1>")
```

The return value is treated intelligently. In this case a string is returned, so it’s turned into a standard response:

```clojure
{:status 200
 :headers {"Content-Type" "text/html; charset=utf-8"}
 :body "<h1>Hello user 1</h1>"}
```

The [compojure.response/Renderable](https://weavejester.github.io/compojure/compojure.response.html) protocol deals with turning a response of an arbitrary type (String, map, File, etc) into a suitable response. It can be overridden to provide custom rendering of your own types.

### Combining routes

Compojure uses the [compojure.core/routes](https://weavejester.github.io/compojure/compojure.core.html#var-routes) function to combine routes. Each route is attempted in order, until a route is found that returns a non-nil response to the request.

```clojure
(def my-routes
  (routes
   (GET "/foo" [] "Hello Foo")
   (GET "/bar" [] "Hello Bar")))
```

Because this is a common pattern, Compojure also provides a `defroutes` macro:

```clojure
(defroutes my-routes
  (GET "/foo" [] "Hello Foo")
  (GET "/bar" [] "Hello Bar"))
```
