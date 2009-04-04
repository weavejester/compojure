(ns test.compojure.http.response
  (:use compojure.http.response)
  (:use clojure.contrib.test-is))

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
