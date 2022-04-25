(ns compojure.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [compojure.response :as response]
            [compojure.coercions :as coercions]
            [clout.core :as clout]
            [compojure.core :refer :all]))

(deftest request-destructuring
  (testing "vector arguments"
    ((GET "/foo" [x y]
       (is (= x "bar"))
       (is (= y "baz"))
       nil)
     (-> (mock/request :get "/foo")
         (assoc :params {:x "bar", :y "baz"}))))

  (testing "vector '& more' arguments"
    ((GET "/:x" [x y & more]
       (is (= x "foo"))
       (is (= y "bar"))
       (is (= more {:z "baz"}))
       nil)
     (-> (mock/request :get "/foo")
         (assoc :params {:y "bar", :z "baz"}))))

  (testing "string parameter names"
    ((GET "/:x" [x y & more]
       (is (= x "foo"))
       (is (= y "bar"))
       (is (= more {"z" "baz"}))
       nil)
     (-> (mock/request :get "/foo")
         (assoc :params {"y" "bar", "z" "baz"}))))

  (testing "vector ':as request' arguments"
    (let [req (-> (mock/request :get "/foo")
                  (assoc :params {:y "bar"}))]
      ((GET "/:x" [x :as r]
            (is (= x "foo"))
            (is (= (dissoc r :params :route-params :compojure/route)
                   (dissoc req :params)))
            nil)
       req)))

  (testing "vector 'x :<< coercion' arguments"
    (let [req (mock/request :get "/foo/10")]
      ((GET "/:x/:y" [x y :<< #(Integer/parseInt %)]
         (is (= x "foo"))
         (is (= y 10))
         nil)
       req)))

  (testing "nil coercions"
    (is (not (nil? ((GET "/foo/:x" [x] (str x))
                    (mock/request :get "/foo/bar")))))
    (is (not (nil? ((GET "/foo/:x" [x :<< coercions/as-int] (str x))
                    (mock/request :get "/foo/100")))))
    (is (not (nil? ((GET "/foo/:x" [x :<< #(Boolean/valueOf %)] (str x))
                    (mock/request :get "/foo/false")))))
    (is (nil? ((GET "/foo/:x" [x :<< coercions/as-int] (str x))
               (mock/request :get "/foo/bar")))))

  (testing "nil coercions in contexts"
    (is (not (nil? ((context "/foo/:x" [x] (GET "/" [] (str x)))
                    (mock/request :get "/foo/bar")))))
    (is (not (nil? ((context "/foo/:x" [x :<< coercions/as-int] (GET "/" [] (str x)))
                    (mock/request :get "/foo/100")))))
    (is (not (nil? ((context "/foo/:x" [x :<< #(Boolean/valueOf %)] (GET "/" [] (str x)))
                    (mock/request :get "/foo/false")))))
    (is (nil? ((context "/foo/:x" [x :<< coercions/as-int] (GET "/" [] (str x)))
               (mock/request :get "/foo/bar")))))

  (testing "map arguments"
    ((GET "/foo" {params :params}
       (is (= params {:x "a", :y "b"}))
       nil)
     (-> (mock/request :get "/foo")
         (assoc :params {:x "a", :y "b"}))))

  (testing "* binding warning"
    (is (= (with-out-str
             (binding [*err* *out*]
               (eval '(compojure.core/GET "/foo/*" [*] (str *)))))
           "WARNING: * should not be used as a route binding.\n"))))

(deftest route-matching
  (testing "_method parameter"
    (let [req (-> (mock/request :post "/foo")
                  (assoc :form-params {"_method" "PUT"}))
          resp {:status 200, :headers {}, :body "bar"}
          route (PUT "/foo" [] resp)]
      (is (= (route req) resp))))

  (testing "_method parameter case-insenstive"
    (let [req (-> (mock/request :post "/foo")
                  (assoc :form-params {"_method" "delete"}))
          resp {:status 200, :headers {}, :body "bar"}
          route (DELETE "/foo" [] resp)]
      (is (= (route req) resp))))

  (testing "_method parameter in multipart forms"
    (let [req (-> (mock/request :post "/foo")
                  (assoc :multipart-params {"_method" "PUT"}))
          resp {:status 200, :headers {}, :body "bar"}
          route (PUT "/foo" [] resp)]
      (is (= (route req) resp))))

  (testing "HEAD requests"
    (let [resp  {:status 200, :headers {"X-Foo" "foo"}, :body "bar"}
          route (GET "/foo" []  resp)]
      (is (= (route (mock/request :head "/foo"))
             (assoc resp :body nil)))))

  (testing "custom regular expressions"
    (let [route (GET ["/foo/:id" :id #"\d+"] [id] id)]
      (is (nil? (route (mock/request :get "/foo/bar"))))
      (is (nil? (route (mock/request :get "/foo/1.1"))))
      (is (route (mock/request :get "/foo/10")))))

  (testing "inline regular expressions"
    (let [route (GET "/foo/:id{\\d+}" [id] id)]
      (is (nil? (route (mock/request :get "/foo/bar"))))
      (is (nil? (route (mock/request :get "/foo/1.1"))))
      (is (route (mock/request :get "/foo/10"))))))

(deftest rfn-test
  (testing "response rendering"
    (is (= ((rfn [] "foo") (mock/request :get "/"))
           (response/render "foo" {}))))

  (testing "head requests"
    (is (= ((rfn [] "foo") (mock/request :head "/"))
           (assoc (response/render "foo" {}) :body nil))))

  (testing "vector binding"
    (is (= ((rfn [id] id) (assoc (mock/request :get "/") :params {"id" "bar"}))
           (response/render "bar" {}))))

  (testing "map binding"
    (is (= ((rfn {x :x} x) (assoc (mock/request :get "/") :x "baz"))
           (response/render "baz" {})))))

(deftest routing-test
  (routing (mock/request :get "/bar")
    (GET "/foo" [] (is false) nil)
    (GET "/bar" [] (is true) nil)))

(deftest routes-test
  ((routes
    (GET "/foo" [] (is false) nil)
    (GET "/bar" [] (is true) nil))
   (mock/request :get "/bar")))

(deftest context-test
  (testing "keyword matching"
    (let [handler (context "/foo/:id" [id] identity)]
      (is (map? (handler (mock/request :get "/foo/10"))))
      (is (nil? (handler (mock/request :get "/bar/10"))))))

  (testing "regex matching"
    (let [handler (context ["/foo/:id" :id #"\d+"] [id] identity)]
      (is (map? (handler (mock/request :get "/foo/10"))))
      (is (nil? (handler (mock/request :get "/foo/ab"))))))

  (testing "symbol matching"
    (let [path    "/foo/:id"
          handler (context path [id] identity)]
      (is (map? (handler (mock/request :get "/foo/10"))))
      (is (nil? (handler (mock/request :get "/bar/10"))))))

  (testing "list matching"
    (let [handler (context (str "/foo" "/:id") [id] identity)]
      (is (map? (handler (mock/request :get "/foo/10"))))
      (is (nil? (handler (mock/request :get "/bar/10"))))))

  (testing "list with regex matching"
    (let [handler (context [(str "/foo" "/:id") :id #"\d+"] [id] identity)]
      (is (map? (handler (mock/request :get "/foo/10"))))
      (is (nil? (handler (mock/request :get "/foo/ab"))))
      (is (nil? (handler (mock/request :get "/bar/10"))))))

  (testing "context key"
    (let [handler (context "/foo/:id" [id] :context)]
      (are [url ctx] (= (handler (mock/request :get url)) ctx)
        "/foo/10"       "/foo/10"
        "/foo/10/bar"   "/foo/10"
        "/foo/10/b%20r" "/foo/10"
        "/bar/10"       nil)))

  (testing "compojure/context key"
    (let [resp               (fn [req bar] {:body [bar (:compojure/context req)] :status 200})
          handler            (GET "/foo/:bar" [bar :as req] (resp req bar))
          cxt-handler        (context "/foo/:bar" [bar] (GET "/" req (resp req bar)))
          in-cxt-handler     (context "/foo" [] (GET "/:bar" [bar :as req] (resp req bar)))
          root-cxt-handler   (context "/" [] (GET "/foo/:bar" [bar :as req] (resp req bar)))
          request            (mock/request :get "/foo/bar") ]
      (is (= (-> request handler :body)          ["bar" nil]))
      (is (= (-> request cxt-handler :body)      ["bar" "/foo/:bar"]))
      (is (= (-> request in-cxt-handler :body)   ["bar" "/foo"]))
      (is (= (-> request root-cxt-handler :body) ["bar" nil]))))

  (testing "compojure/context key in nested context"
    (let [handler (context "/foo" []
                    (context "/:bar" [bar]
                      (GET "/baz" req {:status 200
                                       :body   [bar (:compojure/context req)]})))
          request (mock/request :get "/foo/bar/baz")]
      (is (= (-> request handler :body) ["bar" "/foo/:bar"]))))

  (testing "path-info   key"
    (let [handler (context "/foo/:id" [id] :path-info)]
      (are [url ctx] (= (handler (mock/request :get url)) ctx)
        "/foo/10"       "/"
        "/foo/10/bar"   "/bar"
        "/foo/10/b%20r" "/b%20r"
        "/bar/10"       nil)))

  (testing "routes"
    (let [handler (context "/foo/:id" [id]
                    (GET "/" [] "root")
                    (GET "/id" [] id)
                    (GET "/x/:x" [x] x))]
      (are [url body] (= (:body (handler (mock/request :get url)))
                         body)
        "/foo/10"    "root"
        "/foo/10/"   "root"
        "/foo/10/id" "10"
        "/foo/1/x/2" "2")))

  (testing "url decoding"
    (let [handler        (GET "/ip/:ip" [ip] ip)
          cxt-handler    (context "/ip/:ip" [ip] (GET "/" [] ip))
          in-cxt-handler (context "/ip" [] (GET "/:ip" [ip] ip))
          request        (mock/request :get "/ip/0%3A0%3A0%3A0%3A0%3A0%3A0%3A1%250") ]
      (is (= (-> request handler :body)        "0:0:0:0:0:0:0:1%0"))
      (is (= (-> request cxt-handler :body)    "0:0:0:0:0:0:0:1%0"))
      (is (= (-> request in-cxt-handler :body) "0:0:0:0:0:0:0:1%0"))))

  (testing "url decoding with sensitive characters"
    (let [handler        (GET "/emote/:emote" [emote] emote)
          cxt-handler    (context "/emote/:emote" [emote] (GET "/" [] emote))
          in-cxt-handler (context "/emote" [] (GET "/:emote" [emote] emote))
          request        (mock/request :get "/emote/%5C%3F%2F") ]
      (is (= (-> request handler :body)        "\\?/"))
      (is (= (-> request cxt-handler :body)    "\\?/"))
      (is (= (-> request in-cxt-handler :body) "\\?/"))))

  (testing "root context"
    (let [handler (context "/" []
                    (GET "/" [] "root")
                    (GET "/foo" [] "foo")
                    (GET "/foo/:x" [x] x))]
      (are [url body] (= (:body (handler (mock/request :get url)))
                         body)
        "/"      "root"
        "/foo"   "foo"
        "/foo/2" "2"))))

(deftest let-routes-test
  (let [handler (let-routes [a "foo", b "bar"]
                  (GET "/foo" [] a)
                  (GET "/bar" [] b))]
    (are [url body] (= (:body (handler (mock/request :get url))) body)
        "/foo" "foo"
        "/bar" "bar")))

(deftest make-route-test
  (let [route   (clout/route-compile "/foo/:id")
        handler (make-route :get route #(-> % :params :id))]
    (are [method url body] (= (:body (handler (mock/request method url))) body)
      :get  "/foo/10" "10"
      :post "/foo/10" nil
      :get  "/bar/10" nil
      :get  "/foo"    nil)))

(deftest route-middleware-test
  (let [route      (GET "/foo" [] "foo")
        middleware (fn [_ s] #(response/render s %))
        handler    (wrap-routes route middleware "bar")]
    (testing "route doesn't match"
      (is (nil? (handler (mock/request :get "/bar")))))
    (testing "route matches"
      (is (= (:body (handler (mock/request :get "/foo"))) "bar"))))

  (let [route (routes
               (GET "/foo" [] "foo")
               (GET "/bar" [] "bar"))
        middleware (fn [_ s] #(response/render s %))
        handler    (wrap-routes route middleware "baz")]
    (testing "combined routes don't match"
      (is (nil? (handler (mock/request :get "/baz")))))
    (testing "combined routes match"
      (is (= (:body (handler (mock/request :get "/foo"))) "baz"))
      (is (= (:body (handler (mock/request :get "/bar"))) "baz"))))

  (testing "multiple middleware"
    (let [route   (GET "/" req (str (::a req) (::b req)))
          mw-foo  (fn [h] (fn [r] (h (assoc r ::a "foo"))))
          mw-bar  (fn [h] (fn [r] (h (assoc r ::b "bar"))))
          handler (-> route (wrap-routes mw-foo) (wrap-routes mw-bar))]
      (is (= (:body (handler (mock/request :get "/"))) "foobar"))))

  (testing "middleware setup only once"
    (let [counter    (atom 0)
          middleware (fn [h] (swap! counter inc) h)
          route      (GET "/foo" [] "foo")
          handler    (wrap-routes route middleware)]
      (dotimes [_ 10]
        (handler (mock/request :get "/foo")))
      (is (= @counter 1))))

  (testing "matched route available in request"
    (let [route      (GET "/foo" [] "foo")
          matched    (atom nil)
          middleware (fn [h] (fn [r] (reset! matched (:compojure/route r)) (h r)))
          handler    (wrap-routes route middleware)
          response   (handler (mock/request :get "/foo"))]
      (is (= @matched [:get "/foo"]))))

  (testing "matched route context available in request"
    (let [route      (context "/foo/:bar" [] (GET "/baz" [] "foo"))
          matched    (atom nil)
          middleware (fn [h] (fn [r] (reset! matched (:compojure/context r)) (h r)))
          handler    (wrap-routes route middleware)
          response   (handler (mock/request :get "/foo/bar/baz"))]
      (is (= @matched "/foo/:bar"))))

  (testing "nested route-middlewares are applied in order"
    (let [mw (fn [handler value]
               (fn [req]
                 (let [resp (handler (update req :stack str value))]
                   (update resp :body str value))))
          handler (wrap-routes
                    (routes
                      (wrap-routes (GET "/foo" req (:stack req)) mw "a")
                      (wrap-routes (GET "/bar" req (:stack req)) mw "b"))
                    mw
                    "1")]
      (is (= "1aa1" (:body (handler (mock/request :get "/foo")))))
      (is (= "1bb1" (:body (handler (mock/request :get "/bar"))))))))

(deftest route-information-test
  (let [route (GET "/foo/:id" req req)
        request (route (mock/request :get "/foo/1"))]
    (testing "request has matched route information"
      (is (= (request :compojure/route)
             [:get "/foo/:id"]))))

  (let [route (ANY "/foo/:id" req req)
        request (route (mock/request :post "/foo/1" {}))]
    (testing "ANY request has matched route information"
      (is (= (request :compojure/route)
             [:any "/foo/:id"])))))

(deftest route-async-test
  (testing "single route"
    (let [route (GET "/hello/:name" [name] (str "hello " name))]
      (testing "matching request"
        (let [request   (mock/request :get "/hello/world")
              response  (promise)
              exception (promise)]
          (route request response exception)
          (is (not (realized? exception)))
          (is (= @response
                 {:status  200
                  :headers {"Content-Type" "text/html; charset=utf-8"}
                  :body    "hello world"}))))

      (testing "not-matching request"
        (let [request   (mock/request :get "/goodbye/world")
              response  (promise)
              exception (promise)]
          (route request response exception)
          (is (not (realized? exception)))
          (is (nil? @response))))))

  (testing "multiple routes"
    (let [route (routes
                 (GET "/foo" [] "foo")
                 (GET "/bar" [] "bar")
                 (GET "/baz" [] "baz"))]
      (testing "matching request"
        (let [request   (mock/request :get "/bar")
              response  (promise)
              exception (promise)]
          (route request response exception)
          (is (not (realized? exception)))
          (is (= (:body @response) "bar"))))

      (testing "not-matching URI"
        (let [request   (mock/request :get "/quz")
              response  (promise)
              exception (promise)]
          (route request response exception)
          (is (not (realized? exception)))
          (is (nil? @response))))

      (testing "not-matching method"
        (let [request   (mock/request :post "/bar")
              response  (promise)
              exception (promise)]
          (route request response exception)
          (is (not (realized? exception)))
          (is (nil? @response))))))

  (testing "rfn"
    (let [route     (rfn [name] (str "hello " name))
          request   (-> (mock/request :get "/")
                        (assoc :params {:name "world"}))
          response  (promise)
          exception (promise)]
      (route request response exception)
      (is (not (realized? exception)))
      (is (= @response
             {:status  200
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body    "hello world"}))))

  (testing "context"
    (let [route (context "/:name" [name]
                  (GET "/bar" [] (str name "bar"))
                  (GET "/baz" [] (str name "baz")))]
      (testing "matching request"
        (let [request   (mock/request :get "/foo/baz")
              response  (promise)
              exception (promise)]
          (route request response exception)
          (is (not (realized? exception)))
          (is (= (:body @response) "foobaz"))))

      (testing "not-matching request"
        (let [request   (mock/request :get "/foo/quz")
              response  (promise)
              exception (promise)]
          (route request response exception)
          (is (not (realized? exception)))
          (is (nil? @response)))))

    (testing "with coercion"
      (let [route (context "/:id" [id :<< coercions/as-int]
                    (GET "/" [] (str id)))]
        (testing "matching request"
          (let [request   (mock/request :get "/123")
                response  (promise)
                exception (promise)]
            (route request response exception)
            (is (not (realized? exception)))
            (is (= (:body @response) "123"))))
        (testing "not-matching request"
          (let [request   (mock/request :get "/foo")
                response  (promise)
                exception (promise)]
            (route request response exception)
            (is (not (realized? exception)))
            (is (nil? @response)))))))

  (testing "wrap-routes"
    (let [route (wrap-routes
                 (GET "/foo" [] "foo")
                 (fn [handler]
                   (fn [request respond raise]
                     (handler request
                              #(respond (assoc % ::r (:compojure/route request)))
                              raise))))]
      (testing "matching request"
        (let [request   (mock/request :get "/foo")
              response  (promise)
              exception (promise)]
          (route request response exception)
          (is (not (realized? exception)))
          (is (= (:body @response) "foo"))
          (is (= (::r @response) [:get "/foo"]))))

      (testing "not-matching request"
        (let [request   (mock/request :get "/bar")
              response  (promise)
              exception (promise)]
          (route request response exception)
          (is (not (realized? exception)))
          (is (nil? @response))))))

  (testing "async response"
    (let [route (GET "/" [] (fn [_ respond _] (respond "foobar")))
          request   (mock/request :get "/")
          response  (promise)
          exception (promise)]
      (route request response exception)
      (is (not (realized? exception)))
      (is (= @response
             {:status  200
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body    "foobar"})))))
