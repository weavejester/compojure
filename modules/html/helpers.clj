(compojure/module html)

(def doctype
  {"html4" "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\"
    \"http://www.w3.org/TR/html4/strict.dtd\">\n"

   "xhtml/strict" "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"
    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"

   "xhtml/transitional" "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"})

(defn link-to
  [url title]
  [:a {:href url} title])

(defmacro form-to
  [[method action] & body]
  `[:form {:method '~method :action ~action} ~@body])

(defn label
  [name text]
  [:label {:for (str* name)} text])

(defn text-field
  [name]
  [:input {:type "text" :name (str* name) :id (str* name)}])

(defn submit-tag
  [value]
  [:input {:type "submit" :value value}])

(defn unordered-list
  [coll]
  [:ul
    (domap x coll
      [:li x])])
