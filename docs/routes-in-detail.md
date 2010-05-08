---
layout: doc
title: Routes in Detail
---

Routes in Compojure look something like this:

{% highlight clj %}
(GET "/user/:id" [id]
  (str "<h1>Hello user " id "</h1>"))
{% endhighlight %}

Routes return Ring handler functions. Despite their syntax, there's nothing
magical about them. They just provide a concise way of defining functions to
handle HTTP requests.

### Matching the HTTP method ###

Let's unravel the syntax. The first symbol is:

{% highlight clj %}
GET
{% endhighlight %}

This is one of several route macros Compojure provides. This macro tests the
HTTP request method, and if the method is not "GET", the function returns
`nil`.

Other route macros you can use are `POST`, `PUT`, `DELETE` and `HEAD`. If you
want to match any HTTP method, use the `ANY` macro.

### Matching the URI ###

Next is:

{% highlight clj %}
"/user/:id"
{% endhighlight %}

This is a string that uses the routing syntax defined by [Clout][1]. It has a
lot in common with the routing syntax used in [Ruby on Rails][2] and
[Sinatra][3].

It matches against the URI of the request. The `:id` part will match any
sub-path up to the next "/" or ".", and puts the results in the "id"
parameter.

If we wanted to be more specific, we could also define a custom regular
expression for this parameter:

{% highlight clj %}
["/user/:id", :id #"[0-9]+"]
{% endhighlight %}

Like the HTTP method, if the URI does not match the defined path, the route
function will return `nil`.

### Destructuring the request ###

After the HTTP method and the URI have been matched:

{% highlight clj %}
[id]
{% endhighlight %}

The second argument to the macro provides a way of retrieving information from
the request map. This can either be a vector of parameters you want, or a full
Clojure destructuring form.

In other words, the above syntax binds the symbol `id` to the "id" parameter
in the request map, which in this case was populated by the Clout route string.
We could also use a standard Clojure destructuring form:

{% highlight clj %}
 {{'{'}}{id "id"} :params}
{% endhighlight %}

This provides more control, but is less succinct than the vector syntax.

### Returning a response ###

Once the HTTP request has been matched and destructured, the rest of the route
is encased in an implicit `do` block, just like normal functions:

{% highlight clj %}
(str "<h1>Hello user " id "</h1>"))
{% endhighlight %}

The return value is treated intelligently. In this case a string is returned,
so it's turned into a standard response:

{% highlight clj %}
{:status 200
 :headers {"Content-Type" "text/html"}
 :body "<h1>Hello user 1</h1>"}
{% endhighlight %}

The `compojure.response/render` multimethod deals with turning a response of
an arbitrary type into a suitable response. It can be overriden to provide
custom rendering of your own types.

[1]:http://github.com/weavejester/clout
[2]:http://rubyonrails.org
[3]:http://www.sinatrarb.com
