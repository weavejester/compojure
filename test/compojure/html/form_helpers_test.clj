(ns compojure.html.form-helpers-test
  (:use compojure.html.form-helpers
        clojure.contrib.test-is))

(defn attribute
  "Get an attribute from a tag vector."
  [tag key]
  ((second tag) key))

(deftest test-text-field
  (is (= (text-field :foo)
         [:input {:type "text", :id "foo", :name "foo"}])))

(deftest test-password-field
  (is (= (password-field "foo")
         [:input {:type "password", :id "foo", :name "foo" :value ""}])))

(deftest form-input-attrs
  (let [field (text-field {:style "color: red;"} :foo)]
    (is (= (attribute field :style) "color: red;"))))

(deftest test-with-params
  (is (= (with-params {:foo "bar"} (text-field :foo))
         [:input {:type "text", :id "foo", :name "foo", :value "bar"}])))

(deftest test-check-box
  (is (= (check-box :foo)
         [:input {:type "checkbox"
                  :id "foo"
                  :name "foo"
                  :value "true"
                  :checked nil}])))

(deftest test-radio-button
  (is (= (radio-button :foo)
         [:input {:type "radio"
                  :id "foo_true"
                  :name "foo"
                  :value "true"
                  :checked nil}])))

(deftest test-radio-button-selected
  (is (= (radio-button :foo true "abcdef")
         [:input {:type "radio"
                  :id "foo_abcdef"
                  :name "foo"
                  :value "abcdef"
                  :checked true}])))
  
(deftest select-options-with-values
  (is (= (select-options [["a" "1"] ["b" "2"] ["c" "3"]])
        '([:option {:value "1"} "a"]
          [:option {:value "2"} "b"]
          [:option {:value "3"} "c"]))))

(deftest drop-down-selected
  (is (= (drop-down :foo [["a" "1"] ["b" "2"] ["c" "3"]] "2")
         [:select {:id "foo" :name "foo"}
          '([:option {:value "1"} "a"]
            [:option {:value "2" :selected "selected"} "b"]
            [:option {:value "3"} "c"])])))

(deftest test-form-to
  (let [form (form-to [:post "action"] [])]
    (is (= (attribute form :method) "POST"))))

(deftest test-form-to-update
  (let [form (form-to [:update "action"] [])]
    (is (= (attribute form :method) "POST"))
    (let [hidden (nth form 2)]
      (is (= (attribute hidden :value) "UPDATE"))
      (is (= (attribute hidden :name) "_method"))
      (is (= (attribute hidden :type) "hidden")))))

(deftest test-form-to-attrs
  (let [form (form-to {:class "class" } [:post "action"] [])]
    (is (= (attribute form :class) "class"))))
