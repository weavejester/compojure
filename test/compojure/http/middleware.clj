(ns test.compojure.http.middleware
  (:use compojure.http.middleware)
  (:use compojure.http.routes)
  (:use clojure.contrib.test-is))

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
  (let [routes  (routes (GET "/foo" [{:headers {"a" "b"}} "body"]))
        request {:request-method :get,
                 :uri "/foo"}]
    ((apply f (conj args routes)) request)))

(deftest test-with-headers
  (let [headers {"name1" "value1", "name2" "value2"}
        response (mock-middleware-response with-headers headers)]
    (is (= "value1" (get (:headers response) "name1")))
    (is (= "value2" (get (:headers response) "name2")))))

(deftest test-with-headers-wont-overwrite
  (let [headers {"a" "c"}
        response (mock-middleware-response with-headers headers)]
    (is (= "b" (get (:headers response) "a")))))

(deftest test-with-cache-control
  (let [m {:max-age 3600 :public false :must-revalidate true}]
    (let [response (mock-middleware-response with-cache-control m)]
      (is (= "max-age=3600, must-revalidate"
             (get (:headers response) "Cache-Control"))))))
