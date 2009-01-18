;; compojure.validation
;;
;; Functions for validating form parameters.

(ns compojure.validation
  (:use compojure.control)
  (:use compojure.html.form-helpers)
  (:use compojure.html.page-helpers)
  (:use clojure.contrib.def))

(defvar *errors* {}
  "Var containing validation errors.")

(load "validation/predicates")

(defn validate 
  "Validate a single parameter, or group of parameters, using a predicate. If
  the predicate fails, a validation error is returned. For a single parameter,
  use the following form:
    (validate params name pred message)
  This will use the value of (pred (params name)) to determine if the parameter
  is valid. For multiple parameters:
    (validate params pred message)
  This will use the value of (pred params) to determine validity."
  ([params pred message]
    (if (pred params)
      {}
      {nil [message]}))
  ([params name pred message]
    (if (pred (params name))
      {}
      {name [message]})))

(defn merge-errors
  "Merge a set of validation errors into a single hash map."
  [& results]
  (apply merge-with #(into [] (concat %1 %2)) results))

(defn validation
  "Convinience function to perform a series of validations on a map of params.
  Takes a set of params and a collection of argument vectors for the validate
  function:
  e.g. (validation params
         [name pred message]
         [pred message])
  Is the same as:
       (merge-errors
         (validate params name pred message)
         (validate params pred message))"
  [params & validations]
  (apply merge-errors
    (map #(apply validate params %) validations)))

(defn validation-errors?
  "True if there are errors in the var *errors*."
  []
  (seq *errors*))

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

(defn error-summary
  "Returns a summary of the errors on the form in HTML."
  []
  (unordered-list (apply concat (vals *errors*))))

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
