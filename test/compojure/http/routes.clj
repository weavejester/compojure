(ns test.compojure.http.routes
  (:use fact)
  (:use compojure.http.routes))

(fact "Routes can match HTTP method"
  [method `(GET POST PUT HEAD DELETE)]
  (let [route      (eval `(~method "/" "passed"))
        req-method (-> method name .toLowerCase keyword)]
    (= (route {:request-method req-method, :uri "/"})
       "passed")))

(fact "The ANY route matches any HTTP method"
  [method  [:get :post :put :head :delete]]
  (= ((ANY "/" "passed") {:request-method method, :uri "/"})
     "passed"))

(fact "Routes can match fixed paths"
  [path ["/"
         "/foo"
         "/foo/bar"
         "/foo.txt"]]
  (match-uri (compile-uri-matcher path) path))

(fact "Nil routes are treated as '/'"
  []
  (match-uri (compile-uri-matcher "/") nil))

(fact "Routes can contain keywords"
  [[route path] {"/:x"         "/foo"
                 "/bar/:x"     "/bar/foo"
                 "/:x/bar"     "/foo/bar"
                 "/bar/:x/baz" "/bar/foo/baz"
                 "/:x.txt"     "/foo.txt"
                 "/bar.:x"     "/bar.foo"}]
  (= (match-uri (compile-uri-matcher route) path)
     {:x "foo"}))

(fact "Routes can contain keywords containing hyphens"
  [[route path] {"/:foo-bar"                "/baz"
		 "/aaa/:foo-bar/bbb"        "/aaa/baz/bbb"}]
  (= (match-uri (compile-uri-matcher route) path)
     {:foo-bar "baz"}))

(fact "Routes can contain keywords containing hyphens many times"
  [[route path] {"/:foo-bar/bbb/:foo-bar"   "/baz/bbb/baz"}]
  (= (match-uri (compile-uri-matcher route) path)
     {:foo-bar ["baz" "baz"]}))

(fact "Routes can contain the same keyword many times"
  [[route path] {"/:x/:x/:x"     "/foo/bar/baz"
                 "/a/:x/b/:x.:x" "/a/foo/b/bar.baz"}]
  (= (match-uri (compile-uri-matcher route) path)
     {:x ["foo" "bar" "baz"]}))

(fact "Routes can match wildcards"
  [[route path] {"/*"     "/foo/bar.txt"
                 "/baz/*" "/baz/foo/bar.txt"
                 "/*/baz" "/foo/bar.txt/baz"
                 "/a/*/b" "/a/foo/bar.txt/b"
                 "*"      "foo/bar.txt"}]
  (= (match-uri (compile-uri-matcher route) path)
     {:* "foo/bar.txt"}))

(fact "Keywords are stored in the route map"
  [keyword [:foo :bar :baz]]
  (let [route `(GET ~(str "/" keyword) (~'route ~keyword))]
    (= ((eval route) {:request-method :get, :uri "/foo"})
       "foo")))

(fact "Wildcards are stored in the route map"
  [path ["" "foo" "foo/bar" "foo.bar"]]
  (let [route `(GET "/*" (~'route :*))]
    (= ((eval route) {:request-method :get, :uri (str "/" path)})
       path)))

(fact "Routes can match paths in vars"
  [path ["/foo" "/bar" "/foo/bar"]]
  (= ((GET path "passed") {:request-method :get, :uri path})
     "passed"))
