(ns test.compojure.http.routes
  (:use fact.core)
  (:use fact.random-utils)
  (:use compojure.http.routes))

(def http-methods [:get :post :put :delete])
(def http-macros `(GET POST PUT DELETE))

(fact "Routes can match HTTP methods"
  [method http-methods
   macro  http-macros
   body   random-str]
  (let [route    (eval `(~macro "/" ~body))
        response (route {:request-method method, :uri "/"})]
    (= (:body response) body)))

(fact "The ANY route matches any HTTP method"
  [method http-methods
   body   random-str]
  (let [route    (ANY "/" body)
        response (route {:request-method method, :uri "/"})]
    (= (:body response) body)))

(fact "Routes can match fixed paths"
  [[path & _] #"/(\w+)(/\w+)*(\.\w+)?"]
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
  [[route path] {"/:foo-bar"         "/baz"
                 "/aaa/:foo-bar/bbb" "/aaa/baz/bbb"}]
  (= (match-uri (compile-uri-matcher route) path)
     {:foo-bar "baz"}))

(fact "Routes can contain keywords containing hyphens many times"
  [[route path] {"/:foo-bar/bbb/:foo-bar" "/baz/bbb/baz"}]
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

(fact "Routes can match paths in vars"
  [path ["/foo" "/bar" "/foo/bar"]]
  (let [route    (GET path "passed")
        response (route {:request-method :get, :uri path})]
    (= (:body response) "passed")))

(fact "Keywords are stored in the route-params map"
  [kw [:foo :bar :baz]]
  (let [route    (GET (str "/" kw) (route-params kw))
        response (route {:request-method :get, :uri "/foo"})]
    (= (:body response) "foo")))

(fact "Wildcards are stored in the route-params map"
  [path ["" "foo" "foo/bar" "foo.bar"]]
  (let [route    (GET "/*" (route-params :*))
        response (route {:request-method :get, :uri (str "/" path)})]
    (= (:body response) path)))
