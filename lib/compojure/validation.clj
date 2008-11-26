(ns compojure.validation
    (:use compojure.html
          clojure.contrib def))

(defvar *errors* {}
  "Validation errors var.")

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

(defn merge-errors
  "Merge a set of validation errors into a single hash map."
  [& results]
  (apply merge-with #(into [] (concat %1 %2)) results))

(defn validation-errors?
  "True if there are errors in the var *errors*."
  []
  (seq *errors*))

(defn error-summary []
  "Returns a summary of the errors on the form in HTML."
  (when (validation-errors?)
    [:div.error-summary
      [:p "the page had the following errors:"
        (unordered-list (apply concat (vals *errors*)))]]))

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

(defn error-class
  "Decorator function that marks an input field with an error class if the
  parameter has errors."
  [func]
  (fn [name & args]
    (let [errors (*errors* name)
          result (apply func name args)]
      (if (seq errors)
        [:div.error result]
        result))))
