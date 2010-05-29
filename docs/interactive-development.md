---
layout: doc
title: Interactive Development
---

One of the great advantages of programming in Clojure is being able to
develop software interactively. With Clojure, you can see the effects of your
changes to the source code just by reloading the namespace. There is no need
to restart the environment, or spend time waiting on lengthy compile times.

To do this in Compojure, we need to ensure the Ring adapter doesn't block the
interpreter. We can do this by placing the adapter in a background thread.

Second, we need to ensure that if we reload the handler namespace, the adapter
will take code from the updated handler. Rather than passing the handler
function directly to the adapter, we can pass a var instead, which will always
refer to the latest handler.

Here is an example:

{% highlight clj %}
(future (run-jetty (var your-app) {:port 8080}))
{% endhighlight %}

Any time the `your-app` handler function changes, those changes will be
displayed automatically next time you refresh your browser.
