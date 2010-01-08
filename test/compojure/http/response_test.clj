(ns compojure.http.response-test
  (:use compojure.http.response
        clojure.contrib.test-is))

(deftest nil-response
  (is (= (create-response {} nil)
         {:status 200, :headers {}})))

(deftest int-response
  (is (= (:status (create-response {} 404))
         404)))
         
(deftest next-response
  (is (nil? (create-response {} :next))))

(deftest string-response
  (is (= (:body (create-response {} "Lorem Ipsum"))
         "Lorem Ipsum")))

(deftest seq-response
  (is (= (:body (create-response {} (list "a" "b" "c")))
         (list "a" "b" "c"))))

(deftest map-response
  (let [response {:status  200
                  :headers {"Content-Type" "text/plain"}
                  :body    "Lorem Ipsum"}]
    (is (= (create-response {} response) response))))

(deftest vector-string-response
  (is (= (:body (create-response {} ["Foo" "Bar" "Baz"]))
         "FooBarBaz")))

(deftest vector-int-response
  (is (= (:status (create-response {} [200 500 403]))
         403)))

(deftest default-content-type
  (let [response {:headers {"Foo" "Bar"}}]
    (is (= (:headers (update-response {} response "Foo"))
           {"Foo" "Bar" "Content-Type" "text/html"}))))

(deftest supplied-content-type
  (let [response {:headers {"Content-Type" "text/plain" "Foo" "Bar"}}]
    (is (= (:headers (update-response {} response "Foo"))
           {"Content-Type" "text/plain" "Foo" "Bar"}))))
