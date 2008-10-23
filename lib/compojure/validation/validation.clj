
(ns compojure.validation
    (:use compojure.html)
    (:use clojure.contrib.test-is)
    (:use clojure.contrib.seq-utils))

(def validation-errors {})

(load "predicates.clj")

(defn validate [pred param-name params message]
  "validate a single parameter. pred is a function that takes one
argument, the value of the parameter with name param-name. If pred
returns false or nil, message will be added as a validation error."
     (when-not (pred (params param-name))
       {param-name message}))

(defn validate-params [pred params message]
  "Validate the relationship between two or more parameters. If the
validation only depends on one argument, use validate. pred is a
function that takes one argument, the params map. If pred returns
false or nil, message will be added as a validation error."
  (when-not (pred params)
    {nil message}))

(defn validator [& results]
  "takes a list of calls to validate and validate-params. Returns a map of validation errors"
  (with-local-vars [errors {}]
    (doseq result results ; each validator call returns a map
      (doseq kv result ; for each kv pair
	(when kv
	  (var-set errors (assoc (var-get errors) (key kv)
				 (conj (get (var-get errors) (key kv) []) (val kv)))))))
    (var-get errors)))

(deftest test-validator-good
  (let [params {:username "Bob", :subject "this is 26 characters long"}]
    (println "params = " params)
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
	   {:username ["must not be blank"],
	    :subject ["must be <= 15 characters long",
		      "must be <= 10 characters long"]}))))

(deftest test-validate-param
  (is (= (validate present? :username {:username "Bob"} "must not be blank") 
	 nil))
  (is (= (validate present? :username {} "must not be blank") 
	 {:username "must not be blank"})))

(deftest test-validate-page
  (let [foo-greater-bar? (fn [params] 
			   (> (params :foo) (params :bar)))]
      (is (= (validate-params foo-greater-bar? {:foo 5 :bar 3} "foo must be greater than bar")
	     nil))
      (is (= (validate-params foo-greater-bar? {:foo 4 :bar 6} "foo must be greater than bar")
	     {nil "foo must be greater than bar"}))))
      
(defn decorate-errors [param-name & html-body]
  (if (and 
	(contains? validation-errors param-name) 
	(> (count (validation-errors param-name)) 0))
     [:div {:class "FormError"}
      (vec (cons :ul 
       (map (fn [err] 
	      [:li err]) (validation-errors param-name))))
      html-body]
     html-body))
  
(deftest test-decorate-errors
  (let [dummy-html (list [:ul [:li "a"] [:li "b"]][:p "some more html"])]
    (binding [validation-errors {}]
      (is (= (decorate-errors :foo dummy-html) 
	     (list dummy-html))))
    (binding [validation-errors {:bar ["unrelated error"]}]
      (is (= (decorate-errors :foo dummy-html) 
	     (list dummy-html))))
    (binding [validation-errors {:foo ["foo error one"]}]
      (is (= (decorate-errors :foo dummy-html) 
	     [:div {:class "FormError"} [:ul [:li "foo error one"]] (list dummy-html)])))
    (binding [validation-errors {:foo ["foo error one" "second error"]}]
      (is (= (decorate-errors :foo dummy-html)
	     [:div {:class "FormError"} [:ul [:li "foo error one"] [:li "second error"]] (list dummy-html)])))))

(defn validation-error-summary []
  "displays a div with the summary of errors on the page"
  (when (> (count validation-errors) 0)
     [:div {:class "FormErrorSummary"}
	    [:p "the page had the following errors:"
	     (vec (cons :ul
			(map (fn [err] [:li err]) (flatten (vals validation-errors)))))]]))

(deftest test-validation-error-summary
  (binding [validation-errors {}]
    (is (= nil (validation-error-summary))))
  (binding [validation-errors {nil ["page error one", "page error two"], :foo ["foo error"]}]
    (is (= [:div {:class "FormErrorSummary"} [:p "the page had the following errors:" [:ul [:li "page error one"] [:li "page error two"] [:li "foo error"]]]] (validation-error-summary)))))

(defmacro html-with-validator [arg & html-body]
  `{:html (fn [params#]
	    (html ~@html-body)), :validator ~arg})

(defn get-validation-errors [html-struct params]
  ((html-struct :validator) params))
  
(defn valid-html? [html-struct params]
  (zero? (count (get-validation-errors html-struct params))))
