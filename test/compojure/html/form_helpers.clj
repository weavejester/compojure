(ns test.compojure.html.form-helpers
  (:use compojure.html.form-helpers)
  (:use clojure.contrib.test-is))

(defn attribute
  "Get an attribute from a tag vector."
  [tag key]
  ((second tag) key))

(deftest test-text-field
  (is (= (text-field :foo)
         [:input {:type "text", :id "foo", :name "foo"}])))

(deftest form-input-attrs
  (let [field (text-field {:style "color: red;"} :foo)]
    (is (= (attribute field :style) "color: red;"))))

(deftest test-with-params
  (is (= (with-params {:foo "bar"} (text-field :foo))
         [:input {:type "text", :id "foo", :name "foo", :value "bar"}])))
         
(deftest select-options-with-values
  (is (= (select-options [["a" "1"] ["b" "2"] ["c" "3"]])
        '([:option {:value "1"} "a"]
          [:option {:value "2"} "b"]
          [:option {:value "3"} "c"]))))
