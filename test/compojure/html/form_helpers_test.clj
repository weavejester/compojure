(ns compojure.html.form-helpers-test
  (:use compojure.html.form-helpers
        clojure.contrib.test-is))

(defn attribute
  "Get an attribute from a tag vector."
  [tag key]
  ((second tag) key))

(deftest test-hidden-field
  (testing "passing in only name"
    (is (= [:input {:type "hidden", :name "foo", :id "foo"}]
           (hidden-field "foo"))))
  (testing "passing in name and value"
    (is (= [:input {:value "hidden", :type "hidden", :name "foo", :id "foo"}]
           (hidden-field "foo" "hidden")))))

(deftest test-text-field
  (testing "passing in only name"
    (is (= [:input {:type "text", :id "foo", :name "foo"}]
           (text-field :foo))))
  (testing "passing in name and value"
    (is (= [:input {:value :text-field, :type "text", :name "foo", :id "foo"}]
           (text-field :foo :text-field)))))

(deftest test-password-field
  (is (= [:input {:type "password", :id "foo", :name "foo" :value ""}]
         (password-field "foo"))))

(deftest test-check-box
  (testing "passing in only name"
    (is (= [:input {:type "checkbox" :id "foo" :name "foo" :value "true" :checked nil}]
           (check-box :foo))))
  (testing "passing in name and checked"
    (is (= [:input {:type "checkbox", :name "foo", :id "foo", :value "true", :checked true}]
           (check-box :foo true))))
  (testing "passing in name, checked, and value"
    (is (= [:input {:type "checkbox", :name "foo", :id "foo", :value "checkbox", :checked false}]
           (check-box :foo false "checkbox")))))

(deftest test-radio-button
  (testing "passing in only name"
    (is (= [:input {:type "radio" :id "foo_true" :name "foo" :value "true" :checked nil}]
           (radio-button :foo))))
  (testing "passing in name and checked"
    (is (= [:input {:type "radio", :name "foo", :id "foo_true", :value "true", :checked true}]
           (radio-button :foo true))))
  (testing "passing in name, checked, and value"
    (is (= [:input {:type "radio", :name "foo", :id "foo_radio", :value "radio", :checked false}]
           (radio-button :foo false "radio")))))

(deftest test-select-options
  (testing "passing in only options"
    (is (= '([:option {:value "1"} "a"]
             [:option {:value "2"} "b"]
             [:option {:value "3"} "c"])
           (select-options [["a" "1"] ["b" "2"] ["c" "3"]]))))
  (testing "passing in options and selected"
    (is (= '([:option {:selected "selected" :value "1"} "a"]
             [:option {:value "2"} "b"])
           (select-options [["a" "1"] ["b" "2"]] "1")))))

(deftest test-drop-down
  (testing "passing in name and options"
    (is (= [:select {:name "foo", :id "foo"}
            '([:option {:value "1"} "a"]
              [:option {:value "2"} "b"])]
           (drop-down :foo [["a" "1"] ["b" "2"]]))))
  (testing "passing in name, options, and selected"
    (is (= [:select {:id "foo" :name "foo"}
            '([:option {:value "1"} "a"]
                [:option {:value "2" :selected "selected"} "b"]
                  [:option {:value "3"} "c"])]
           (drop-down :foo [["a" "1"] ["b" "2"] ["c" "3"]] "2")))))

(deftest test-text-area
  (testing "passing in only name"
    (is (= [:textarea {:name "text", :id "text"} nil]
           (text-area "text"))))
  (testing "passing in name and value"
    (is (= [:textarea {:name "text", :id "text"} "textarea"]
           (text-area "text" "textarea")))))

(deftest test-label
  (is (= [:label {:for "label"} "labeltext"]
         (label "label" "labeltext"))))

(deftest test-submit-button
  (is (= [:input {:type "submit", :value "submit"}]
         (submit-button "submit"))))

(deftest test-reset-button
  (is (= [:input {:type "reset", :value "reset"}]
         (reset-button "reset"))))

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

(deftest form-input-attrs
  (let [field (text-field {:style "color: red;"} :foo)]
    (is (= (attribute field :style) "color: red;"))))

(deftest test-with-params
  (is (= (with-params {:foo "bar"} (text-field :foo))
         [:input {:type "text", :id "foo", :name "foo", :value "bar"}])))