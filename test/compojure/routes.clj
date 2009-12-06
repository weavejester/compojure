(ns test.compojure.routes
  (:use clojure.contrib.test-is)
  (:use compojure.routes)
  (:use test.helpers))

(deftest assoc-route-map
  (is (= (assoc-route-params {:params {}} {"foo" "bar"})
         {:route-params {"foo" "bar"}, :params {"foo" "bar"}})))

(deftest assoc-route-vector
  (is (= (assoc-route-params {:params {}} ["foo" "bar"])
         {:route-params ["foo" "bar"], :params {}})))

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

(deftest route-match-form-method
  (let [routes  (routes (DELETE "/foo" "body"))
        request {:request-method :post
                 :uri            "/foo"
                 :content-type   "application/x-www-form-urlencoded"
                 :body           (input-stream "_method=DELETE&a=1")}]
    (is (= (:status (routes request))
           200))))

(deftest route-not-match-form-method
  (let [routes  (routes (DELETE "/foo" "body"))
        request {:request-method :post
                 :uri            "/foo"
                 :content-type   "application/x-www-form-urlencoded"
                 :body           (input-stream "a=1")}]
    (is (nil? (routes request)))))

(deftest route-match-form-method-not-post
  (let [routes  (routes (POST "/foo" "post") (DELETE "/foo" "delete"))
        request {:request-method :post
                 :uri            "/foo"
                 :content-type   "application/x-www-form-urlencoded",
                 :body           (input-stream "_method=DELETE&a=1")}]
    (is (= (:body (routes request))
           "delete"))))

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
