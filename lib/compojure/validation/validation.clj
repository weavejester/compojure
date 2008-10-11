
(ns compojure.validation)
    ;(:use compojure.html))

(def validator-functions [])
(def validation-errors {})

(defn acceptance-pred [params name message]
  (not (nil? (params name))))

(defn validate-acceptance [params name message]
  (let [result (not (nil? (params name)))]
    (when (not result)
      (set! validation-errors (assoc validation-errors name message)))
    result))

(defn validate-not-blank [params name message]
  (let [result (and (not (nil? (params name))) (not (= "" (params name))))]
    (when (not result)
      (set! validation-errors (assoc validation-errors name message)))
    result))

(defn seq-contains? [seq val]
  (contains? (set seq) val))

(defn validate-in [params name lst message]
  (let [result (seq-contains? lst (params name))]
    (when (not result)
      (set! validation-errors (assoc validation-errors name message)))
    result))

(defmacro decorate-errors [param-name & html-body]
  `(if (contains? validation-errors ~param-name)
     [:div {:class "FormError"} 
      (validation-errors ~param-name) 
      ~@html-body]
     ~@html-body))
  
(defn validation-error-summary []
  "displays a div with the summary of errors on the page"
  (when (> (count validation-errors) 0)
     [:div {:class "FormErrorSummary"}
	    [:p "the page had the following errors:"
	     [:ul
	      (map (fn [err] [:li err]) (vals validation-errors))]]]))

(defmacro html-with-validator [arg & html-body]
  `{:html (fn [] (html ~@html-body)), :validator ~arg})

(defn get-validation-errors [html-struct params]
  (binding [validation-errors {}]
    ((html-struct :validator) params)
    validation-errors))

(defn valid-html? [html-struct params]
  (zero? (count (get-validation-errors html-struct params))))

