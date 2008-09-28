;; compojure.html
;;
;; Compojure library for generating HTML or XML output from a tree of vectors.
;;
;; A small example of the syntax:
;; 
;;   (html
;;     [:html
;;       [:head
;;         [:title "Hello World"]]
;;       [:body
;;         [:h1.big "Hello World"]
;;         [:img {:src "test.png"}]
;;         [:ul#letters
;;           (domap letter '(a b c d)
;;             [:li letter])]]])

(ns compojure.html
  (:use (compojure str-utils)
        (clojure.contrib seq-utils))
  (:import (clojure.lang Sequential)))

(defn- optional-attrs
  "Adds an optional attribute map to the supplied function's arguments."
  [func]
  (fn [attrs & body]
    (if (map? attrs)
      (let [[tag func-attrs & body] (apply func body)]
        (apply vector tag (merge func-attrs attrs) body))
      (apply func attrs body))))

(load "form_functions.clj")
(load "page_functions.clj")

(defn escape-html
  "Change special characters into HTML character entities."
  [string]
  (.. (str string)
    (replaceAll "&"  "&amp;")
    (replaceAll "<"  "&lt;")
    (replaceAll ">"  "&gt;")
    (replaceAll "\"" "&quot;")))

(def h escape-html)    ; Shortcut for escaping HTML

(defn- map-to-attrs
  "Turn a map into a string of XML attributes."
  [attrs]
  (str-map
    (fn [[key val]]
      (str* " " key "=\"" (h val) "\""))
    attrs))

(def xml)

(defn- create-tag
  "Wrap some content in an XML tag."
  [tag attrs content]
  (str* "<" tag (map-to-attrs attrs) ">"
          content
        "</" tag ">"))

(defn- create-closed-tag
  "Make a closed XML tag with no content."
  [tag attrs]
  (str* "<" tag (map-to-attrs attrs) " />"))

(defn- map-xml
  "Turn a list of XML into a string using a supplied formatter."
  [format trees]
  (str-map (partial xml format) trees))

(defn- raw-xml-formatter
  "Format XML without any indentation or extra whitespace."
  [next-format tag attrs & body]
  (if body
    (create-tag tag attrs (map-xml next-format body))
    (create-closed-tag tag attrs)))

(defn- indented-xml-formatter
  "Format XML with indentation and with no 'single' tags."
  [next-format tag attrs & body]
  (str (if body
         (let [content (map-xml next-format body)]
           (create-tag tag attrs (str "\n" (indent content))))
         (create-tag tag attrs ""))
       "\n"))

(defn- expand-seqs
  "Expand out all the sequences in a collection."
  [coll]
  (mapcat #(if (seq? %) % (list %)) coll))

(defn- ensure-attrs
  "Ensure the tag has a map of attributes."
  [[tag & body]]
  (if (map? (first body))
    (list* tag body)
    (list* tag {} body)))

(defn xml
  "Turns a tree of vectors into a string of XML. Any sequences in the
  tree are expanded out."
  ([tree]
    (xml raw-xml-formatter tree))
  ([format tree]
    (if (vector? tree)
      (apply format format
        (expand-seqs (ensure-attrs tree)))
      (str tree))))

(def #^{:private true
        :doc "A set of HTML tags that should be rendered as blocks"}
  html-block-tags
  #{:blockquote :body :div :dl :fieldset :form :head :html :ol
    :p :pre :table :tbody :tfoot :thead :tr :script :select :ul})

(def #^{:private true
        :doc "HTML tags that should be rendered on their own line"}
  html-line-tags
  #{:br :dd :dt :h1 :h2 :h3 :h4 :h5 :h6 :hr :li :link
    :option :td :textarea :title})

(defn- parse-css-tag
  "Pulls the id and class attributes from a tag name formatted in a CSS style.
  e.g. :div#content -> [:div  {:id \"content\"}]
       :span.error  -> [:span {:class \"error\"}]"
  [tag attrs]
  (let [word  "([^\\s\\.#]+)"
        lexer (str word "(#" word ")?" "(\\." word ")?")
        [_ tag _ id _ class]
              (re-matches (re-pattern lexer) (str* tag))
        attrs (merge attrs
                (if id    {:id id})
                (if class {:class class}))]
    [tag attrs]))

(defn- html-formatter
  "Format HTML in a readable fashion."
  [next-format tag attrs & body]
  (let [[tag attrs] (parse-css-tag tag attrs)
        tag         (keyword tag)
        format      (if (contains? html-block-tags tag)
                      indented-xml-formatter
                      raw-xml-formatter)
        content     (apply format html-formatter tag attrs body)]
    (if (contains? html-line-tags tag)
      (str content "\n")
      content)))

(defn html
  "Nicely formats a tree of vectors into HTML."
  [& trees]
  (map-xml html-formatter trees))
