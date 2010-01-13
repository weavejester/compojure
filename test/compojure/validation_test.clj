(ns compojure.validation-test
  (:use compojure.html.form-helpers
        compojure.validation
        clojure.contrib.test-is))

(deftest passes-validate
  (is (= (validate {:a 1} :a (constantly true) "fail")
         {})))

(deftest fails-validate
  (is (= (validate {:a 1} :a (constantly false) "fail")
         {:a ["fail"]})))

(deftest error-class-errors
  (binding [*errors* {:foo "bar"}]
    (is (= ((error-class text-field) :foo)
           [:div.error (text-field :foo)]))))

(deftest error-class-no-errors
  (binding [*errors* {}]
    (is (= ((error-class text-field) :foo)
           (text-field :foo)))))

(deftest merge-errors-test
  (are (= (apply merge-errors _1) _2)
    [{}]                     {}
    [{} {} {}]               {}
    [{:a ["f"]}]             {:a ["f"]}
    [{:a ["f"]} {:b ["g"]}]  {:a ["f"], :b ["g"]}
    [{:a ["f"]} {:a ["g"]}]  {:a ["f" "g"]}))

(deftest validation-test
  (let [params {:a 1, :b 2}
        pred   #(= % 2)
        mesg   "isn't 2"]
    (is (= (validation params [:a pred mesg] [:b pred mesg])
           {:a ["isn't 2"]}))))
