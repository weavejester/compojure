# Common Problems

This page lists some pitfalls that may not be obvious to users. Feel free to add your own!

### A middleware tries to read the `:body` of a request, but finds it empty

* In a request, the value for `:body` is an `InputStream`. InputStream is mutable and can only ever be read once. Anything that tries to read the `InputStream` after it has already been read will fail to do so. If you find the value for `:body`
 empty when it shouldn't be, the most likely cause is some other middleware higher in the stack reading the `InputStream` before it gets to you. 
    * Ex: If you have two sets of routes in the same stack each using the `wrap-params` middleware. Only the first routes would have the params.

### How do I make trailing slashes optional for a route?
    
* Compojure does not [yet](https://github.com/weavejester/compojure/issues/68) automatically redirect requests for URIs with trailing slashes to handlers matching non-trailing-slash routes. For now, consider using a middleware function to chomp the slash from the end of the URI or to call a handler which redirects to the proper route. 
    * Ex: [trailing-slash-middleware](https://gist.github.com/dannypurcell/8215411)
