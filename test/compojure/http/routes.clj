(ns test.compojure.http.routes
  (:use compojure.http.routes)
  (:use clojure.contrib.test-is)
  (:use test.helpers))

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
    "/:foo-"    "/baz" {:foo- "baz"}))

(deftest same-keyword-many-times
  (are (= (match-uri (compile-uri-matcher _1) _2) _3)
    "/:x/:x/:x" "/a/b/c" {:x ["a" "b" "c"]}
    "/:x/b/:x"  "/a/b/c" {:x ["a" "c"]}))

(deftest wildcard-paths
  (are (= (match-uri (compile-uri-matcher _1) _2) _3)
    "/*"     "/foo"         {:* "foo"}
    "/*"     "/foo.txt"     {:* "foo.txt"}
    "/*"     "/foo/bar"     {:* "foo/bar"}
    "/foo/*" "/foo/bar/baz" {:* "bar/baz"}
    "/a/*/d" "/a/b/c/d"     {:* "b/c"}))

(deftest url-paths
  (is (match-uri (compile-uri-matcher "http://localhost")
                 "http://localhost")))

(deftest url-port-paths
  (let [matcher (compile-uri-matcher "localhost:8080")]
    (is (match-uri matcher "localhost:8080"))
    (is (not (match-uri matcher "localhost:7070")))))

(deftest unmatched-paths
  (is (nil? (match-uri (compile-uri-matcher "/foo") "/bar"))))

(deftest regex-paths
  (is (match-uri #"/[A-Z][a-z]" "/Ab"))
  (is (not (match-uri  #"/[A-Z][a-z]" "/ab"))))

(deftest regex-path-params
  (are (= (match-uri _1 _2) _3)
    #"/foo/(\w+)"   "/foo/bar" ["bar"]
    #"/(\w+)/(\d+)" "/foo/10"  ["foo" "10"]))

(deftest route-response
  (let [route    (GET "/" "Lorem Ipsum")
        request  {:request-method :get, :uri "/"}
        response (route request)]
    (is (= response {:status 200, 
                     :headers {"Content-Type" "text/html"}, 
                     :body "Lorem Ipsum"}))))

(defn- route-body
  [route method uri]
  (:body (route {:request-method method, :uri uri})))

(deftest route-methods
  (are (= (route-body _1 _2 "/") _3)
    (GET    "/" "a") :get    "a"
    (POST   "/" "b") :post   "b"
    (PUT    "/" "c") :put    "c"
    (HEAD   "/" "d") :head   "d"
    (DELETE "/" "e") :delete "e"))

(deftest route-any
  (are (= (route-body (ANY "/" _2) _1 "/") _2)
    :get    "a"
    :post   "b"
    :put    "c"
    :delete "d"))

(deftest route-var-paths
  (let [path "/foo/bar"]
    (is (= (route-body (GET path "pass") :get path)
           "pass"))))

(deftest route-not-match
  (let [route   (GET "/" "Lorem Ipsum")
        request {:request-method :get, :uri "/foo"}]
    (is (nil? (route request)))))

(deftest route-keywords
  (let [route (GET "/:foo"
                (is (= (:route-params request) {:foo "bar"}))
                "")]
    (route {:request-method :get, :uri "/bar"})))

(deftest combine-routes
  (let [r1 (fn [req] (if (= (:uri req) "/") {:body "x"}))
        r2 (fn [req] {:body "y"})
        rs (routes r1 r2)]
    (is (rs {:uri "/"}) "x")
    (is (rs {:uri "/foo"}) "y")))

(deftest route-params
  (let [site (routes
               (GET "/:route"
                 (is (= (params :route) "yes"))
                 (is (= (params :query) "yes"))
                 (is (= (params :form)  "yes"))
                 (is (request :params) params)
                 :next))]
    (site (merge 
            {:request-method :get
             :uri "/yes"
             :query-string "query=yes"}
            (form-request "form=yes")))))
