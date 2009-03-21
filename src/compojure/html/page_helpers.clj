;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.html.page-helpers:
;; 
;; Functions for generating document and header boilerplate.

(ns compojure.html.page-helpers
  (:use compojure.control)
  (:use compojure.html.gen)
  (:use compojure.str-utils)
  (:use clojure.contrib.str-utils)
  (:import java.net.URLEncoder))

(def doctype
  {:html4
   (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" "
        "\"http://www.w3.org/TR/html4/strict.dtd\">\n")

   :xhtml-strict
   (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
        "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")

   :xhtml-transitional
   (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
        "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n")})

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
  (domap [script scripts]
    [:script {:type "text/javascript" :src script}]))

(defn include-css
  "Include a list of external stylesheet files."
  [& styles]
  (domap [style styles]
    [:link {:type "text/css" :href style :rel "stylesheet"}]))

(defn javascript-tag
  "Wrap the supplied javascript up in script tags and a CDATA section."
  [script]
  [:script {:type "text/javascript"}
    (str "//<![CDATA[\n" script "\n//]]>")])

(defn link-to
  "Wraps some content in a HTML hyperlink with the supplied URL."
  [url & content]
  [:a {:href url} content])

(defn url-encode
  "Encodes a single string or sequence of key/value pairs."
  [string-or-map]
  (let [enc #(URLEncoder/encode (str* %))]
    (if (string? string-or-map)
      (enc string-or-map)
      (str-join "&"
        (map (fn [[key val]] (str (enc key) "=" (enc val)))
             string-or-map)))))

(defn url-params
  "Encodes a map of parameters and adds them onto the end of an existing
  address.
  e.g. (url-params \"http://example.com\" {:lang \"en\", :offset 10})
       => \"http://example.com?lang=en&offset=10\""
  [address param-map]
  (str address "?" (url-encode param-map)))

(defn unordered-list
  "Wrap a collection in an unordered list"
  [coll]
  [:ul {}
    (domap [x coll]
      [:li x])])

(defn ordered-list
  "Wrap a collection in an unordered list"
  [coll]
  [:ol {}
    (domap [x coll]
      [:li x])])

(decorate-with optional-attrs
  xhtml-tag
  link-to
  unordered-list
  ordered-list)
