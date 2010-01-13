(ns compojure.http.middleware-test
  (:use compojure.http.middleware
        compojure.http.routes
        clojure.contrib.test-is))

(deftest test-header-option
  (is (= (header-option [:name "value"])
         "name=value")))

(deftest test-header-option-true
  (is (= (header-option [:name true])
         "name")))

(deftest test-header-option-false
  (is (nil? (header-option [:name false]))))

(deftest test-header-options-multi
  (let [m {:name "value",
           :false false,
           :true true}]
    (is (= (header-options m ", ")
           "name=value, true"))))

(deftest test-header-options-single
  (let [m {:name "value"}]
    (is (= (header-options m ", ")
           "name=value"))))

(defn mock-middleware-response [f & args]
  (let [routes  (routes (GET "/foo" [{:headers {"k1" "v1" "k2" "v2"}} "body"]))
        request {:request-method :get,
                 :uri "/foo"}]
    ((apply f (conj args routes)) request)))

(deftest test-with-headers
  (let [headers {"name1" "value1", "name2" "value2"}
        response (mock-middleware-response with-headers headers)]
    (is (= "value1" (get (:headers response) "name1")))
    (is (= "value2" (get (:headers response) "name2")))
    (is (= "v1" (get (:headers response) "k1")))))

(deftest test-with-headers-overwrites
  (let [headers {"k1" "vnew"}
        response (mock-middleware-response with-headers headers)]
    (is (= "vnew" (get (:headers response) "k1")))
    (is (= "v2" (get (:headers response) "k2")))))

(deftest test-with-cache-control
  (let [m {:max-age 3600 :public false :must-revalidate true}]
    (let [response (mock-middleware-response with-cache-control m)]
      (is (= "max-age=3600, must-revalidate"
             (get (:headers response) "Cache-Control"))))))

(defn run-ignore-trailing-slash-paths
  [route-path uri]
  (let [routes  (routes (GET route-path "foo"))
        request {:request-method :get
                 :uri uri}
        response ((ignore-trailing-slash routes) request)]
    (= (:body response) "foo")))

(deftest test-ignore-trailing-slash-paths
  (are (run-ignore-trailing-slash-paths _1 _2)
       "/" "/"
       "/foo" "/foo"
       "/foo" "/foo/"
       "/foo/bar" "/foo/bar/"))

(defn run-with-context
  [route-path uri context]
  (let [routes  (routes (GET route-path "foo"))
        request {:request-method :get
                 :uri uri}
        response ((with-context routes context) request)]
    (= (:body response) "foo")))

(deftest test-with-context
  (are (run-with-context _1 _2 "/context")
       "/" "/context"
       "/home" "/context/home"
       "/asset/1" "/context/asset/1"))

(deftest test-without-context
  (are (not (run-with-context _1 _2 "/context"))
       "/" "/"
       "/home" "/home"
       "/asset/1" "/asset/1"))

(defn run-mimetypes
  [uri type options]
  (let [routes  (routes (GET uri "foo"))
        request {:request-method :get
                 :uri uri}
        response ((with-mimetypes routes options) request)
        result (get (:headers response) "Content-Type")]
    (= type result)))

(deftest test-with-default-mimetypes
  (are (run-mimetypes _1 _2 {})
       "/" "text/html"
       "/foobar" "text/html"
       "/file.pdf" "application/pdf"
       "/files/bar.css" "text/css"))

(deftest test-with-custom-mimetypes
  (let [options {:mimetypes {"foo" "test/foo"
                             "bar" "test/bar"}
                 :default "test/default"}]
    (are (run-mimetypes _1 _2 options)
         "/" "test/default"
         "/foobar" "test/default"
         "/file.pdf" "test/default"
         "/file.foo" "test/foo"
         "/files/file.bar" "test/bar")))