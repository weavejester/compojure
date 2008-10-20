
(ns compojure.validation
    (:use compojure.html)
    (:use clojure.contrib.test-is)
    (:use clojure.contrib.seq-utils))

(def validator-functions [])
(def validation-errors {})

(load "predicates.clj")

(defn- add-error [param-name message]
  (println "adding error " param-name " " message)
  (set! validation-errors (assoc validation-errors param-name 
				 (conj (get validation-errors param-name []) message))))

(deftest test-add-error
  (binding [validation-errors {}]
    (add-error :foo "this is an error")
    (is (= ["this is an error"] (validation-errors :foo)))
    (add-error :foo "a second error")
    (is (= ["this is an error" "a second error"] (validation-errors :foo)))
    (add-error :bar "an error on bar")
    (is (= ["this is an error" "a second error"] (validation-errors :foo))) ;test that this doesn't interfere with the original errors
    (is (= ["an error on bar"] (validation-errors :bar)))  ; and that we can get our error on bar
    (add-error nil "this error is on the whole page")
    (is (= ["this error is on the whole page"] (validation-errors nil))))) 

(defn validate [pred param-name params message]
  "validate a single parameter. pred is a function that takes one
argument, the value of the parameter with name param-name. If pred
returns false or nil, message will be added as a validation error."
     (when-not (pred (params param-name))
       (add-error param-name message)))

(defn validate-params [pred params message]
  "Validate the relationship between two or more parameters. If the
validation only depends on one argument, use validate. pred is a
function that takes one argument, the params map. If pred returns
false or nil, message will be added as a validation error."
  (when-not (pred params)
    (add-error nil message)))

(deftest test-validate-param
  (binding [validation-errors {}]
    (validate present? :username {:username "Bob"} "must not be blank")
    (is (= nil (validation-errors :username))))

  (binding [validation-errors {}]
    (validate present? :username {} "must not be blank")
    (is (= ["must not be blank"] (validation-errors :username)))))

(deftest test-validate-page
  (let [foo-greater-bar? (fn [params] 
			   (> (params :foo) (params :bar)))
	foo-times-bar-odd? (fn [params]
			    (odd? (* (params :foo) (params :bar))))]

    (binding [validation-errors {}]
      (validate foo-greater-bar? {:foo 5 :bar 3} "foo must be greater than bar")
      (is (= nil (validation-errors nil)))
      (validate foo-greater-bar? {:foo 4 :bar 6} "foo must be greater than bar")
      (is (= ["foo must be greater than bar"] (validation-errors nil)))
      (validate foo-times-bar-odd? {:foo 3 :bar 4} "foo times bar must be odd")
      (is (= ["foo must be greater than bar", "foo times bar must be odd"] (validation-errors nil))))))

(defmacro decorate-errors [param-name & html-body]
  `(if (and 
	(contains? validation-errors ~param-name) 
	(> (count (validation-errors ~param-name)) 0))
     [:div {:class "FormError"}
      (vec (cons :ul 
       (map (fn [err#] 
	      [:li err#]) (validation-errors ~param-name))))
      ~@html-body]
     ~@html-body))
  
(deftest test-decorate-errors
  (let [dummy-html [:ul [:li "a"] [:li "b"]]]
    (binding [validation-errors {}]
      (is (= dummy-html (decorate-errors :foo dummy-html))))
    (binding [validation-errors {:bar "unrelated error"}]
      (is (= dummy-html (decorate-errors :foo dummy-html))))
    (binding [validation-errors {:foo ["foo error one"]}]
      (is (= [:div {:class "FormError"} [:ul [:li "foo error one"]] dummy-html] (decorate-errors :foo dummy-html))))
    (binding [validation-errors {:foo ["foo error one" "second error"]}]
      (is (= [:div {:class "FormError"} [:ul [:li "foo error one"] [:li "second error"]] dummy-html] (decorate-errors :foo dummy-html))))))

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
  `{:html (fn [] (html ~@html-body)), :validator ~arg})

(defn get-validation-errors [html-struct params]
  (binding [validation-errors {}]
    ((html-struct :validator) params)
    validation-errors))

(defn valid-html? [html-struct params]
  (zero? (count (get-validation-errors html-struct params))))

(defmacro defhtml [name let-binding & body]
  `(defn ~name {:html (fn [params]
			(let ~@let-binding
			  (html ~@body)))}))

(defn render 
  ([html-struct options]
     (cond
      (options :validate) 
          (binding [compojure.validation/validation-errors (get-validation-errors html-struct (options :params))]
	    (render html-struct (dissoc options :validate)))
       true
       ((html-struct :html))))
  ([html-struct]
     (render html-struct {})))
