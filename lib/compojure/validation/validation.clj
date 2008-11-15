
(ns compojure.validation
    (:use compojure.html)
    (:use clojure.contrib.seq-utils))

(def *errors* {})
(def *params* {})

(load "predicates.clj")

(defn validate [pred param-name params message]
  "validate a single parameter. pred is a function that takes one
argument, the value of the parameter with name param-name. If pred
returns false or nil, message will be added as a validation error."
     (if (pred (params param-name))
       {}
       {param-name [message]}))

(defn validate-params [pred params message]
  "Validate the relationship between two or more parameters. If the
validation only depends on one argument, use validate. pred is a
function that takes one argument, the params map."
  (when-not (pred params)
    {nil [message]}))

(defn validator [& results]
  "merge a set of validation results"
  (apply merge-with #(into [] (concat %1 %2)) results))

(defn decorate-errors [param-name & html-body]
  "default decoration for validation errors"
  (if-let errors (seq (*errors* param-name))
    [:div.error
     (unordered-list errors)
      html-body]
     html-body))
  
(defn error-summary []
  "displays a div with the summary of errors on the page"
  (when (> (count *errors*) 0)
     [:div.errorSummary
	    [:p "the page had the following errors:"
	     (unordered-list (flatten (vals *errors*)))]]))

(defn valid-params? [validate-fn params]
  (zero? (count (validate-fn params))))

(defmacro with-params
  "Bind of params to *params*"
  [params & body]
  `(binding [*params* ~params]
    ~@body))

(defmacro with-validation
  "Binds *errors* to (validation-fn *params*)"
  [validation-fn & body]
  `(binding [*errors* (~validation-fn *params*)]
    ~@body)) 

(defmacro with-validated-params
  "Equivalent to (with-params params (with-validation validation-fn))"
  [params validation-fn & body]
  `(with-params ~params
     (with-validation ~validation-fn
       ~@body))) 

(defn validation-errors? []
  (seq *errors*))


