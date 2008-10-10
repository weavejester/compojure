
(ns compojure.validation
    (:use compojure.html))

(def validator-functions [])
(def validation-errors {})

(defn acceptance-predicate [val]
  (not (nil? val)))

(defn seq-contains? [seq val]
  (contains? (set seq) val))

(defn in-pred [lst val]
  (seq-contains? lst val))

(defn not-blank-pred [val]
  (not (nil? val)))

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

;; (defn html-with-validator [arg & html-body]
;;   {:html html-body, :validator arg})

(defmacro html-with-validator [arg & html-body]
  `{:html (fn [] (html ~@html-body)), :validator ~arg})

(defn get-validation-errors [html-struct params]
     ((html-struct :validator) params))

(defn valid-html? [html-struct params]
  (zero? (count (get-validation-errors html-struct params))))

(defn render 
  ([html-struct options]
     (println "render. options = " options)
     (cond
      (options :validate) 
          (binding [compojure.validation/validation-errors (get-validation-errors html-struct (options :params))]
	    (println "inside binding, validation-errors = " validation-errors)
	    (render html-struct (dissoc options :validate)))
       true
       (do 
	 (println "rendering, validation errors = " validation-errors)
	 ((html-struct :html)))))
  ([html-struct]
     (println "render no options")
     (render html-struct {})))
      

(defn always-true [params]
  {})

(defn always-false [params]
  {"" "lots of errors!"})

(def test-html
  (html-with-validator always-true
      [:head
       "header stuff"]
      [:body
       [:p
	(System/currentTimeMillis)]]))

;(defn validate-user [params]
;  (validate-not-blank params :name "Must have a name")
;  (validate-zipcode params :zipcode))
