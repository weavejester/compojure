(ns test.compojure.http.routes
  (:use fact)
  (:use compojure.http.routes))

(fact "Routes can match HTTP method"
  [method `(GET POST PUT HEAD DELETE)]
  (let [route `(~method "/" "passed")]
    (= ((eval route) (name method) "/")
       "passed")))

(fact "The ANY route matches any HTTP method"
  [method '(GET POST PUT HEAD DELETE)]
  (= ((ANY "/" "passed") (name method) "/")
     "passed"))

(fact "Routes can match fixed paths"
  [path ["/"
         "/foo"
         "/foo/bar"
         "/foo.txt"]]
  (match-path (compile-path-matcher path) path))

(fact "Routes can contain keywords"
  [[route path] {"/:x"         "/foo"
                 "/bar/:x"     "/bar/foo"
                 "/:x/bar"     "/foo/bar"
                 "/bar/:x/baz" "/bar/foo/baz"
                 "/:x.txt"     "/foo.txt"
                 "/bar.:x"     "/bar.foo"}]
  (= (match-path (compile-path-matcher route) path)
     {:x "foo"}))

(fact "Routes can contain the same keyword many times"
  [[route path] {"/:x/:x/:x"     "/foo/bar/baz"
                 "/a/:x/b/:x.:x" "/a/foo/b/bar.baz"}]
  (= (match-path (compile-path-matcher route) path)
     {:x ["foo" "bar" "baz"]}))

(fact "Routes can match wildcards"
  [[route path] {"/*"     "/foo/bar.txt"
                 "/baz/*" "/baz/foo/bar.txt"
                 "/*/baz" "/foo/bar.txt/baz"
                 "/a/*/b" "/a/foo/bar.txt/b"
                 "*"      "foo/bar.txt"}]
  (= (match-path (compile-path-matcher route) path)
     {:* "foo/bar.txt"}))

(fact "Keywords are stored in the route map"
  [keyword [:foo :bar :baz]]
  (let [route `(GET ~(str "/" keyword) (~'route ~keyword))]
    (= ((eval route) "GET" "/foo")
       "foo")))

(fact "Wildcards are stored in the route map"
  [path ["" "foo" "foo/bar" "foo.bar"]]
  (let [route `(GET "/*" (~'route :*))]
    (= ((eval route) "GET" (str "/" path))
       path)))

(fact "Routes can match paths in vars"
  [path ["/foo" "/bar" "/foo/bar"]]
  (= ((GET path "passed") "GET" path)
     "passed"))
