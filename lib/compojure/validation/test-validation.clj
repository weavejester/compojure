(in-ns 'compojure.validation)
(use 'clojure.contrib.test-is)

(deftest test-validate-param
  (is (= (validate present? :username {:username "Bob"} "must not be blank") 
	 {}))
  (is (= (validate present? :username {} "must not be blank") 
	 {:username ["must not be blank"]})))

(deftest test-validator-good
  (let [params {:username "Bob", :subject "this is 26 characters long"}]
    (is (= (validator
	    (validate present? :username params "must not be blank"))
	   {}))))

(deftest test-validator-one-error
  (let [params {:username nil, :subject "this is 26 characters long"}]
	(is (= (validator
		   (validate present? :username params "must not be blank")
		   (validate (max-length 30) :subject params "must be <= 30 characters long"))
	       {:username ["must not be blank"]}))))

(deftest test-validator-two-errors
  (let [params {:username nil, :subject "this is 26 characters long"}]
    (is (= (validator
	    (validate present? :username params "must not be blank")
	    (validate (max-length 15) :subject params "must be <= 15 characters long")
	    (validate (max-length 10) :subject params "must be <= 10 characters long"))
	   {:username ["must not be blank"]
	    :subject ["must be <= 15 characters long",
		      "must be <= 10 characters long"]}))))


(deftest test-validate-page
  (let [foo-greater-bar? (fn [params] 
			   (> (params :foo) (params :bar)))]
      (is (= (validate-params foo-greater-bar? {:foo 5 :bar 3} "foo must be greater than bar")
	     nil))
      (is (= (validate-params foo-greater-bar? {:foo 4 :bar 6} "foo must be greater than bar")
	     {nil ["foo must be greater than bar"]}))))

(deftest test-error-summary
  (binding [*errors* {}]
    (is (= nil (error-summary))))
  (binding [*errors* {nil ["page error one", "page error two"], :foo ["foo error"]}]
    (is (= (error-summary) [:div.errorSummary [:p "the page had the following errors:" [:ul {} (list [:li "page error one"] [:li "page error two"] [:li "foo error"])]]]))))

(deftest test-decorate-errors
  (let [dummy-html (list [:ul [:li "a"] [:li "b"]][:p "some more html"])]
    (binding [*errors* {}]
      (is (= (decorate-errors :foo dummy-html) 
	     (list dummy-html))))
    (binding [*errors* {:bar ["unrelated error"]}]
      (is (= (decorate-errors :foo dummy-html) 
	     (list dummy-html))))
    (binding [*errors* {:foo ["foo error one"]}]
      (is (= (decorate-errors :foo dummy-html) 
	     [:div.error [:ul {} (list [:li "foo error one"])] (list dummy-html)])))
    (binding [*errors* {:foo ["foo error one" "second error"]}]
      (is (= (decorate-errors :foo dummy-html)
	     [:div.error [:ul {} (list [:li "foo error one"] [:li "second error"])] (list dummy-html)])))))
