;; Helper functions for generating common HTML elements
(in-ns 'compojure.html)

(use '(compojure control str-utils)
     '(clojure.contrib seq-utils))

(def #^{:private true}
  *static* (ref ""))

(defn set-static-prefix
  [prefix]
  (dosync (ref-set *static* prefix)))

(def doctype
  {:html4
    "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\"
    \"http://www.w3.org/TR/html4/strict.dtd\">\n"

   :xhtml-strict
    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"
    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"

   :xhtml-transitional
    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"})

(defn link-to
  [url title]
  [:a {:href url} title])

(defmacro form-to
  [[method action] & body]
  (if (includes? method ['GET 'POST])
    `[:form {:method '~method :action ~action} ~@body]
    `[:form {:method "POST" :action ~action}
       (hidden-field "_method" '~method)
       ~@body]))

(defn form-fn
  [script & body]
  [:form {:onsubmit (str script "(this); return false")} body])

(defn label
  [name text]
  [:label {:for (str* name)} text])

(defn text-field
  [name]
  [:input {:type "text" :name (str* name) :id (str* name)}])

(defn text-area
  [name]
  [:textarea {:name (str* name) :id (str* name)} ""])

(defn hidden-field
  [name value]
  [:input {:type  "hidden"
           :name  (str* name)
           :id    (str* name)
           :value value}])

(defn submit-tag
  [value]
  [:input {:type "submit" :value value}])

(defn image-tag
  [src]
  [:img {:src (str @*static* "/images/" src)}])

(defn unordered-list
  [coll]
  [:ul
    (domap x coll
      [:li x])])

(defn javascript-tag
  [script]
  [:script {:type "text/javascript"} script])

(defn include-js
  [& scripts]
  (domap script scripts
    [:script {:type "text/javascript"
              :src  (str @*static* "/javascript/" script ".js")}]))

(defn include-css
  [& styles]
  (domap style styles
    [:link {:type "text/css"
            :href (str @*static* "/stylesheet/" style ".css")}]))

(defn xhtml-tag
  [lang & contents]
  [:html {:xmlns "http://www.w3.org/1999/xhtml"
          "xml:lang" lang
          :lang lang}
    contents])
