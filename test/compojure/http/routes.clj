(ns test.compojure.http.routes
  (:use compojure.http.routes)
  (:use clojure.contrib.test-is))

(deftest fixed-path
  (are (match-uri (compile-uri-matcher _1) _1)
    "/"
    "/foo"
    "/foo/bar"
    "/foo/bar.html"))

(deftest nil-paths
  (is (match-uri (compile-uri-matcher "/") nil)))

(deftest keyword-paths
  (are (= (match-uri (compile-uri-matcher _1) _2) _3)
    "/:x"       "/foo"     {:x "foo"}
    "/foo/:x"   "/foo/bar" {:x "bar"}
    "/a/b/:c"   "/a/b/c"   {:c "c"}
    "/:a/b/:c"  "/a/b/c"   {:a "a", :c "c"}))

(deftest keywords-match-extensions
  (are (= (match-uri (compile-uri-matcher _1) _2) _3)
    "/foo.:ext" "/foo.txt" {:ext "txt"}
    "/:x.:y"    "/foo.txt" {:x "foo", :y "txt"}))

(deftest hyphen-keywords
  (are (= (match-uri (compile-uri-matcher _1) _2) _3)
    "/:foo-bar" "/baz" {:foo-bar "baz"}
    "/:foo-"    "/baz" {:foo- "baz"}
    "/:-foo"    "/baz" {:-foo "baz"}))

(deftest same-keyword-many-times
  (are (= (match-uri (compile-uri-matcher _1) _2) _3)
    "/:x/:x/:x" "/a/b/c" {:x ["a" "b" "c"]}
    "/:x/b/:x"  "/a/b/c" {:x ["a" "c"]}))


(deftest route-get-method
  (let [route    (GET "/" "foobar")
        request  {:request-method :get, :uri "/"}
        response (route request)]
    (is (= (:body response) "foobar"))))

(comment
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

(fact "Keywords are stored in (request :route-params)"
  [kw [:foo :bar :baz]]
  (let [route    (GET (str "/" kw) (-> request :route-params kw))
        request  {:request-method :get, :uri "/lorem"}
        response (route request)]
    (= (:body response) "lorem")))

(fact "Wildcards are stored in (request :route-params)"
  [path ["" "foo" "foo/bar" "foo.bar"]]
  (let [route    (GET "/*" (-> request :route-params :*))
        request  {:request-method :get, :uri (str "/" path)}
        response (route request)]
    (= (:body response) path)))

(fact "A shortcut to route parameters is to use params"
  [kw [:foo :bar :baz]]
  (let [route    (GET (str "/" kw) (params kw))
        request  {:request-method :get, :uri "/ipsum"}
        response (route request)]
    (= (:body response) "ipsum")))

(fact "Routes that don't match the path return nil"
  [path #"/\w+.txt"]
  (let [route    (GET "/foo.html" "foobar")
        request  {:request-method :get, :uri path}
        response (route request)]
    (nil? response)))

(fact "Routes can use regular expressions"
  [path #"/\w+"]
  (let [regex    (re-pattern path)
        route    (GET regex "lorem")
        request  {:request-method :get, :uri path}
        response (route request)]
    (= (:body response) "lorem")))

(fact "Regular expressions return route parameters as a vector of groups"
  [[path regex] {"/foo/bar" #"/foo/(.*)"}]
  (let [route    (GET regex ((request :route-params) 0))
        request  {:request-method :get, :uri path}
        response (route request)]
    (= (:body response) "bar")))

  )
