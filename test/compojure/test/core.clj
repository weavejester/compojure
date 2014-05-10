(ns compojure.test.core
  (:use clojure.test
        ring.mock.request
        compojure.core
        compojure.response
        clout.core))

(deftest request-destructuring
  (testing "vector arguments"
    ((GET "/foo" [x y]
       (is (= x "bar"))
       (is (= y "baz"))
       nil)
     (-> (request :get "/foo")
         (assoc :params {:x "bar", :y "baz"}))))

  (testing "vector '& more' arguments"
    ((GET "/:x" [x y & more]
       (is (= x "foo"))
       (is (= y "bar"))
       (is (= more {:z "baz"}))
       nil)
     (-> (request :get "/foo")
         (assoc :params {:y "bar", :z "baz"}))))

  (testing "string parameter names"
    ((GET "/:x" [x y & more]
       (is (= x "foo"))
       (is (= y "bar"))
       (is (= more {"z" "baz"}))
       nil)
     (-> (request :get "/foo")
         (assoc :params {"y" "bar", "z" "baz"}))))

  (testing "vector ':as request' arguments"
    (let [req (-> (request :get "/foo")
                  (assoc :params {:y "bar"}))]
      ((GET "/:x" [x :as r]
            (is (= x "foo"))
            (is (= (dissoc r :params :route-params)
                   (dissoc req :params)))
            nil)
       req)))

  (testing "map arguments"
    ((GET "/foo" {params :params}
       (is (= (params {:x "a", :y "b"})))
       nil)
     (-> (request :get "/foo")
         (assoc :params {:x "a", :y "b"})))))

(deftest route-matching
  (testing "_method parameter"
    (let [req (-> (request :post "/foo")
                  (assoc :form-params {"_method" "PUT"}))
          resp {:status 200, :headers {}, :body "bar"}
          route (PUT "/foo" [] resp)]
      (is (= (route req) resp))))

  (testing "_method parameter case-insenstive"
    (let [req (-> (request :post "/foo")
                  (assoc :form-params {"_method" "delete"}))
          resp {:status 200, :headers {}, :body "bar"}
          route (DELETE "/foo" [] resp)]
      (is (= (route req) resp))))

  (testing "HEAD requests"
    (let [resp  {:status 200, :headers {"X-Foo" "foo"}, :body "bar"}
          route (GET "/foo" []  resp)]
      (is (= (route (request :head "/foo"))
             (assoc resp :body nil)))))

  (testing "custom regular expressions"
    (let [route (GET ["/foo/:id" :id #"\d+"] [id] id)]
      (is (nil? (route (request :get "/foo/bar"))))
      (is (nil? (route (request :get "/foo/1.1"))))
      (is (route (request :get "/foo/10"))))))

(deftest routing-test
  (routing (request :get "/bar")
    (GET "/foo" [] (is false) nil)
    (GET "/bar" [] (is true) nil)))

(deftest routes-test
  ((routes
    (GET "/foo" [] (is false) nil)
    (GET "/bar" [] (is true) nil))
   (request :get "/bar")))

(deftest context-test
  (testing "keyword matching"
    (let [handler (context "/foo/:id" [id] identity)]
      (is (map? (handler (request :get "/foo/10"))))
      (is (nil? (handler (request :get "/bar/10"))))))
  (testing "regex matching"
    (let [handler (context ["/foo/:id" :id #"\d+"] [id] identity)]
      (is (map? (handler (request :get "/foo/10"))))
      (is (nil? (handler (request :get "/foo/ab"))))))
  (testing "symbol matching"
    (let [path    "/foo/:id"
          handler (context path [id] identity)]
      (is (map? (handler (request :get "/foo/10"))))
      (is (nil? (handler (request :get "/bar/10"))))))
  (testing "list matching"
    (let [handler (context (str "/foo" "/:id") [id] identity)]
      (is (map? (handler (request :get "/foo/10"))))
      (is (nil? (handler (request :get "/bar/10"))))))
  (testing "context key"
    (let [handler (context "/foo/:id" [id] :context)]
      (are [url ctx] (= (handler (request :get url)) ctx)
        "/foo/10"       "/foo/10"
        "/foo/10/bar"   "/foo/10"
        "/foo/10/b%20r" "/foo/10"
        "/bar/10"       nil)))
  (testing "path-info   key"
    (let [handler (context "/foo/:id" [id] :path-info)]
      (are [url ctx] (= (handler (request :get url)) ctx)
        "/foo/10"       "/"
        "/foo/10/bar"   "/bar"
        "/foo/10/b%20r" "/b%20r"
        "/bar/10"       nil)))
  (testing "routes"
    (let [handler (context "/foo/:id" [id]
                    (GET "/" [] "root")
                    (GET "/id" [] id)
                    (GET "/x/:x" [x] x))]
      (are [url body] (= (:body (handler (request :get url)))
                         body)
        "/foo/10"    "root"
        "/foo/10/"   "root"
        "/foo/10/id" "10"
        "/foo/1/x/2" "2")))
  (testing "url decoding"
    (let [handler        (GET "/ip/:ip" [ip] ip)
          cxt-handler    (context "/ip/:ip" [ip] (GET "/" [] ip))
          in-cxt-handler (context "/ip" [] (GET "/:ip" [ip] ip))
          request        (request :get "/ip/0%3A0%3A0%3A0%3A0%3A0%3A0%3A1%250") ]
      (is (= (-> request handler :body)        "0:0:0:0:0:0:0:1%0"))
      (is (= (-> request cxt-handler :body)    "0:0:0:0:0:0:0:1%0"))
      (is (= (-> request in-cxt-handler :body) "0:0:0:0:0:0:0:1%0")))))

(deftest let-routes-test
  (let [handler (let-routes [a "foo", b "bar"]
                  (GET "/foo" [] a)
                  (GET "/bar" [] b))]
    (are [url body] (= (:body (handler (request :get url)))
                         body)
        "/foo" "foo"
        "/bar" "bar")))

(deftest make-route-test
  (let [handler (make-route :get (route-compile "/foo/:id") #(-> % :params :id))]
    (are [method url body] (= (:body (handler (request method url)))
                              body)
      :get  "/foo/10" "10"
      :post "/foo/10" nil
      :get  "/bar/10" nil
      :get  "/foo"    nil)))


(deftest context-url-decoding-test
  (testing "url encoded percent"
    (let [handler (GET "/ip/:ip" [ip] ip)
          context-handler (context "/ip/:ip" [ip]
                               (GET "/" [] ip))
          in-context-handler (context "/ip" []
                            (GET "/:ip" [ip] ip))
          request (request :get "/ip/0%3A0%3A0%3A0%3A0%3A0%3A0%3A1%250") ]
      (is (= "0:0:0:0:0:0:0:1%0"
             (-> request handler :body)))
      (is (= "0:0:0:0:0:0:0:1%0"
             (-> request context-handler :body)))
      (is (= "0:0:0:0:0:0:0:1%0"
             (-> request in-context-handler :body))))))
