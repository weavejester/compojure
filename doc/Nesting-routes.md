# Nesting Routes

The `context` macro provides a way of giving a set of routes a common prefix:

```clojure
(defroutes user-routes
  (context "/user/current" []
    (GET "/" [] ...) ;the route that exists at "/user/current"
    (GET "/profile" [] ...)
    (GET "/posts" [] ...)))
```

Route parameters may be added to the context, just like a normal route:

```clojure
(defroutes user-routes
  (context "/user/:user-id" [user-id]
    (GET "/profile" [] ...)
    (GET "/posts" [] ...)))
```

Because routes are closures, the `user-id` symbol is available to use in the two sub routes.

However, if your inner routes are defined separately, you need to manually pass any bound parameters from the context. For example:

```clojure
(defn inner-routes [user-id]
  (routes
   (GET "/profile" [] ...)
   (GET "/posts" [] ...)))
  
(defroutes user-routes
  (context "/user/:user-id" [user-id]
    (inner-routes user-id)))
```

This is because parameters are bound with a lexical, rather than dynamic scope.
