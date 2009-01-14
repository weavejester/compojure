(ns test.compojure.http.routes
  (:use fact)
  (:use compojure.http.routes))

(fact "Routes can match fixed paths"
  [path ["/"
         "/foo"
         "/foo/bar"
         "/foo.txt"]]
  (match-route (compile-route path) path))

(fact "Routes can contain keywords"
  [[route path] {"/:x"         "/foo"
                 "/bar/:x"     "/bar/foo"
                 "/:x/bar"     "/foo/bar"
                 "/bar/:x/baz" "/bar/foo/baz"
                 "/:x.txt"     "/foo.txt"
                 "/bar.:x"     "/bar.foo"}]
  (= (match-route (compile-route route) path)
     {:x "foo"}))

(fact "Routes can contain the same keyword many times"
  [[route path] {"/:x/:x/:x"     "/foo/bar/baz"
                 "/a/:x/b/:x.:x" "/a/foo/b/bar.baz"}]
  (= (match-route (compile-route route) path)
     {:x ["foo" "bar" "baz"]}))

(fact "Routes can match wildcards"
  [[route path] {"/*"     "/foo/bar.txt"
                 "/baz/*" "/baz/foo/bar.txt"
                 "/*/baz" "/foo/bar.txt/baz"
                 "/a/*/b" "/a/foo/bar.txt/b"
                 "*"      "foo/bar.txt"}]
  (= (match-route (compile-route route) path)
     {:* "foo/bar.txt"}))