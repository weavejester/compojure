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
        (clojure.contrib def
                         seq-utils)))

(defvar *params* {}
  "Parameter map var that form input field functions use to populate their
  default values.")

(defmacro with-params
  "Bind a map of params to *params*."
  [params & body]
  `(binding [*params* ~params]
    ~@body))

(defn- input-field
  "Creates a form input field."
  [type name value]
  [:input {:type  type
           :name  (str* name)
           :id    (str* name)
           :value value}])

(defn hidden-field
  "Creates a hidden input field."
  ([name]       (hidden-field name (*params* name)))
  ([name value] (input-field "hidden" name value)))

(defn text-field
  "Creates a text input field."
  ([name]       (text-field name (*params* name)))
  ([name value] (input-field "text" name value)))

(defn password-field
  "Creates a password input field."
  [name]
  (input-field "password" name ""))

(defn check-box
  "Creates a check box."
  ([name]
    (check-box name "1"))
  ([name true-value]
    (input-field "checkbox" name true-value))
  ([name true-value false-value]
    (list (check-box name true-value)
          [:input {:type  "hidden"
                   :name  (str* name)
                   :value false-value}])))

(defn select-options
  "Turn a collection into a set of option tags."
  ([options]
    (select-options options nil))
  ([options selected]
    (let [select (fn [opt attrs]
                   (if (and selected (= opt (str* selected)))
                     (merge attrs {:selected "selected"})
                     attrs))]
      (domap [opt options]
        (if (vector? opt)
          (let [text  (opt 0)
                value (str* (opt 1))]
            [:option (select value {:value value}) text])
          [:option (select opt {}) opt])))))

(defn drop-down
  "Creates a drop-down box using the 'select' tag."
  ([name options]
    (drop-down name options (*params* name)))
  ([name options selected]
    [:select {:name (str* name) :id (str* name)}
      (select-options options selected)]))

(defn text-area
  "Creates a text area element."
  ([name]
    (text-area name (*params* name)))
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

(decorate-with optional-attrs
  hidden-field
  text-field
  check-box
  drop-down
  text-area
  label
  submit-button
  reset-button)

(defmacro decorate-fields
  "Wrap all input field functions in a decorator."
  [decorator & body]
  `(decorate-bind ~decorator
     [text-field
      password-field
      check-box
      drop-down
      text-area]
    (list ~@body)))

(defn form-to*
  [[method action] & body]
  (into []
    (concat
      (if (includes? method ['GET 'POST])
        [:form {:method method :action action}]
        [:form {:method "POST" :action action} (hidden-field "_method" method)])
      body)))

(defmacro form-to
  "Create a form that points to a particular method and route.
  e.g. (form-to [PUT \"/post\"]
         ...)"
  [handler & body]
  (if (map? handler)
    (let [[method action] (first body)
          body            (rest body)]
      `(optional-attrs handler
         (form-to* ['~method ~action] ~@body)))
    (let [[method action] handler]
      `(form-to* ['~method ~action] ~@body))))
