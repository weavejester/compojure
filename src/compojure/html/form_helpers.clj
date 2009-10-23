;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.html.form-helpers
  "Functions for generating HTML forms and input fields."
  (:use compojure.html.gen)
  (:use compojure.control)
  (:use compojure.str-utils)
  (:use clojure.contrib.def)
  (:use clojure.contrib.seq-utils))

;; Global parameters for easy default values

(defvar *params* {}
  "Parameter map var that form input field functions use to populate their
  default values.")

(defmacro with-params
  "Bind a map of params to *params*."
  [params & body]
  `(binding [*params* ~params]
    ~@body))

;; Form input fields

(defn- input-field
  "Creates a form input field."
  [type name value]
  (let [name  (str* name)
        attrs {:type type, :name name, :id name}
        attrs (if value
                (assoc attrs :value value)
                attrs)]
    [:input attrs]))

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
    (check-box name (*params* name)))
  ([name checked?]
    (check-box name checked? "true"))
  ([name checked? value]
    [:input {:type    "checkbox"
             :name    (str* name)
             :id      (str* name)
             :value   value
             :checked checked?}]))

(defn radio-button
 "Creates a radio button."
 ([id]
    (radio-button id (*params* id)))
 ([id checked?]
    (radio-button id checked? "true"))
 ([id checked? value]
    [:input {:type    "radio"
             :name    (str* id "_" value)
             :id      (str* id)
             :value   value
             :checked checked?}]))

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

(defn file-upload
  "Creates a file upload input."
  [name]
  [:input {:type "file", :name (str* name), :id (str* name)}])

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

(defn form-to
  "Create a form that points to a particular method and route.
  e.g. (form-to [:put \"/post\"]
         ...)"
  [[method action] & body]
  (let [method-str (upcase-name method)]
    (into []
      (concat
        (if (includes? [:get :post] method)
          [:form {:method method-str :action action}]
          [:form {:method "POST" :action action}
           (hidden-field "_method" method-str)])
        body))))

(decorate-with optional-attrs
  hidden-field
  text-field
  check-box
  drop-down
  text-area
  file-upload
  label
  submit-button
  reset-button
  form-to)

(defmacro decorate-fields
  "Wrap all input field functions in a decorator."
  [decorator & body]
  `(decorate-bind ~decorator
     [text-field
      password-field
      check-box
      drop-down
      text-area
      file-upload]
    (list ~@body)))
