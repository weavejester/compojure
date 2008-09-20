;; compojure.html (form_functions)
;;
;; Functions for generating HTML form controls.
;;
;; e.g.
;; (form-to [PUT "/post"]
;;   (text-field :subject "New Post")
;;   (text-area  :body)
;;   (submit-button "Post"))

(ns compojure.html
  (:use (compojure control
                   str-utils)
        (clojure.contrib seq-utils)))

(defn- input-field
  "Creates a form input field."
  [type name value]
  [:input {:type  type
           :name  (str* name)
           :id    (str* name)
           :value value}])

(defn hidden-field
  "Creates a hidden input field."
  [name value]
  (input-field "hidden" name value))

(defn text-field
  "Creates a text input field."
  ([name]       (text-field name ""))
  ([name value] (input-field "text" name value)))

(defn check-box
  "Creates a check box."
  ([name true-value]
    (input-field "checkbox" name true-value))
  ([name true-value false-value]
    (list (check-box name true-value)
          [:input {:type  "hidden"
                   :name  (str* name)
                   :value false-value}])))

(defn text-area
  "Creates a text area element."
  ([name] (text-area name ""))
  ([name value]
    [:textarea {:name (str* name) :id (str* name)} value]))

(defn label
  "Create a label for an input field with the supplied name."
  [name text]
  [:label {:for (str* name)} text])

(defn submit-button
  "Create a submit button."
  [text]
  [:input {:type "submit" :value text}])

(defn reset-button
  "Create a form reset button."
  [text]
  [:input {:type "reset" :value text}])

(defn form-to*
  [[method action] & body]
  (if (includes? method ['GET 'POST])
    [:form {:method method :action action} body]
    [:form {:method "POST" :action action}
       (hidden-field "_method" method)
       body]))

(decorate-with optional-attrs
  hidden-field
  text-field
  text-area
  check-box
  label
  submit-button
  reset-button
  form-to*)

(defmacro form-to
  "Create a form that points to a particular method and route.
  e.g. (form-to [PUT \"/post\"]
         ...)"
  [handler & body]
  `(form-to* ~handler ~@body))
