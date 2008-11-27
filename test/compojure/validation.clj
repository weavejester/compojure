(ns test.compojure.validation
  (:use fact
        compojure.validation))

(fact "Parameters that pass validation return empty maps"
  []
  (= (validate {:a 1} :a (constantly true) "fail")
     {}))

(fact "Parameters that fail validation return error message maps"
  [[param message] {:a  "fail"
                    'b  "error"
                    "c" ""}]
  (= (validate {param 1} param (constantly false) message)
     {param [message]}))

(fact "Error message maps can be merged with merge-errors"
  [[in out] {[{}]                     {}
             [{} {} {}]               {}
             [{:a ["f"]}]             {:a ["f"]}
             [{:a ["f"]} {:b ["g"]}]  {:a ["f"], :b ["g"]}
             [{:a ["f"]} {:a ["g"]}]  {:a ["f" "g"]}}]
  (= (apply merge-errors in)
     out))

(fact "The validation function is a short for merge-errors and many validates"
  [params [{:a 1, :b 2, :c 3}
           {}
           {:a 1, :c 3}
           {:a 3, :c 1}
           {:a 3, :c 1, :b 2}
           {:foo 10}
           {:a "foo"}]]
  (= (validation params
       [:a (partial = 1) "a isn't 1"]
       [:b (partial = 2) "b isn't 2"]
       [:c (partial = 3) "c isn't 3"]
       [#(= (count %) 3) "size isn't 3"])
     (merge-errors
       (validate params :a (partial = 1) "a isn't 1")
       (validate params :b (partial = 2) "b isn't 2")
       (validate params :c (partial = 3) "c isn't 3")
       (validate params #(= (count %) 3) "size isn't 3"))))
