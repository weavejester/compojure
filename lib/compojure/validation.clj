(ns compojure.validation
    (:use compojure.html)
    (:require [clojure.contrib.seq-utils :as seq-utils])
    (:require [compojure.str-utils :as str-utils]))

(def *errors* {})

(load "validation/predicates")

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

(defn error-summary []
  "displays a div with the summary of errors on the page"
  (when (> (count *errors*) 0)
     [:div.errorSummary
	    [:p "the page had the following errors:"
	     (unordered-list (seq-utils/flatten (vals *errors*)))]]))

(defn valid-params? [validate-fn params]
  (zero? (count (validate-fn params))))

(defmacro with-validation
  "Binds *errors* to (validation-fn *params*)."
  [validation-fn & body]
  `(binding [*errors* (~validation-fn *params*)]
    ~@body)) 

(defmacro with-validated-params
  "Equivalent to (with-params params (with-validation validation-fn))."
  [params validation-fn & body]
  `(with-params ~params
     (with-validation ~validation-fn
       ~@body))) 

(defn validation-errors? []
  (seq *errors*))

(defn decorate-errors
  "called when there is a validation error. "
  [normal-output errors]
  (html 
   [:div.error
   (unordered-list errors)
	normal-output]))

(defn error-class
  "Mark an input field with an error class if the parameter has errors."
  [func]
  (fn [name & args]
    (let [errors (*errors* name)
          result (apply func name args)]
      (if (seq errors)
        [:div.error result]
        result)))) 
