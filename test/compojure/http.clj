(ns test.compojure.http
  (:use fact
        (compojure http)))

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

(fact "Routes can match wildcards"
  [[route path] {"/*"     "/foo/bar.txt"
                 "/baz/*" "/baz/foo/bar.txt"
                 "/*/baz" "/foo/bar.txt/baz"
                 "/a/*/b" "/a/foo/bar.txt/b"
                 "*"      "foo/bar.txt"}]
  (= (match-route (compile-route route) path)
     {:* "foo/bar.txt"}))

(fact "Routes can be raw regular expressions"
  [[route path] {#"/(foo)"     "/foo"
                 #"/([a-z]+)"  "/foo"
                 #"/.*?/(foo)" "/bar/baz/foo"}]
  (= (match-route route path)
     ["foo"]))
