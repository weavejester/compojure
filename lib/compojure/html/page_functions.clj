;; compojure.html (page_functions)
;;
;; Functions for generating document and header boilerplate.

(ns compojure.html
  (:use (compojure control
                   str-utils)))

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

(defn xhtml-tag
  "Create an XHTML tag for the specified locale.
   e.g. (xhtml \"en\"
          [:head ...]
          [:body ...])"
  [lang & contents]
  [:html {:xmlns "http://www.w3.org/1999/xhtml"
          "xml:lang" lang
          :lang lang}
    contents])

(defn include-js
  "Include a list of external javascript files."
  [& scripts]
  (domap script scripts
    [:script {:type "text/javascript" :src script}]))

(defn include-css
  "Include a list of external stylesheet files."
  [& styles]
  (domap style styles
    [:link {:type "text/css" :href style}]))

(defn javascript-tag
  "Wrap the supplied javascript up in script tags and a CDATA section."
  [script]
  [:script {:type "text/javascript"}
    (str "//<![CDATA[\n" script "\n//]]>")])

(defn link-to
  "Link some page content to another URL."
  ([url & content]
     [:a {:href url} content])
  ([url content param-map]
     (link-to (str* url "?" 
		    (str-join "&" (map (fn [pair] (str* (first pair) "=" (second pair))) param-map))) content)))


(defn unordered-list
  "Wrap a collection in an unordered list"
  [coll]
  [:ul {}
    (domap x coll
      [:li x])])

(defn ordered-list
  "Wrap a collection in an unordered list"
  [coll]
  [:ol {}
    (domap x coll
      [:li x])])

(decorate-with optional-attrs
  xhtml-tag
  link-to
  unordered-list
  ordered-list)
