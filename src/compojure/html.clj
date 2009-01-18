;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.html:
;; 
;; A library for generating HTML output from a tree of vectors. The first item
;; of the vector is the tag name, the optional second item is a hash of
;; attributes, and the rest is the body of the tag.

(ns compojure.html
  (:use compojure.str-utils)
  (:use clojure.contrib.def))

(defn optional-attrs
  "Adds an optional attribute map to the supplied function's arguments."
  [func]
  (fn [attrs & body]
    (if (map? attrs)
      (let [[tag func-attrs & body] (apply func body)]
        (apply vector tag (merge func-attrs attrs) body))
      (apply func attrs body))))

(defn escape-html
  "Change special characters into HTML character entities."
  [string]
  (.. (str string)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(defvar h escape-html
  "Shortcut for escape-html")

(defn- map-to-attrs
  "Turn a map into a string of HTML attributes, sorted by attribute name."
  [attrs]
  (map-str
    (fn [[key val]]
      (if key
        (str " " key "=\"" (h val) "\"")))
    (sort
      (map (fn [[key val]]
             (cond
               (true? val) [(str* key) (str* key)]
               (not val)   [nil nil]
               :else       [(str* key) (str* val)]))
           attrs))))

(defn- create-tag
  "Wrap some content in an HTML tag."
  [tag attrs content]
  (str* "<" tag (map-to-attrs attrs) ">"
          content
        "</" tag ">"))

(defn- create-closed-tag
  "Make a closed XML tag with no content."
  [tag attrs]
  (str* "<" tag (map-to-attrs attrs) " />"))

(defn- expand-seqs
  "Expand out all the sequences in a collection."
  [coll]
  (mapcat
    #(if (or (seq? %) (nil? %))
       %
       (list %))
    coll))

(defn- ensure-attrs
  "Ensure the tag has a map of attributes."
  [[tag & body]]
  (if (map? (first body))
    (list* tag body)
    (list* tag {} body)))

(defvar- css-lexer #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(defn- parse-css-tag
  "Pulls the id and class attributes from a tag name formatted in a CSS style.
  e.g. :div#content -> [:div  {:id \"content\"}]
       :span.error  -> [:span {:class \"error\"}]"
  [tag attrs]
  (let [[_ tag id classes] (re-matches css-lexer (str* tag))
        attrs (merge attrs
                (if id {:id id})
                (if classes
                  {:class (.replace classes "." " ")}))]
    [tag attrs]))

(declare html)

(defvar- container-tags
  #{:body :b :dd :div :dl :dt :em :fieldset :form :h1 :h2 :h3 :h4 :h5 :h6 :head
    :html :i :label :li :ol :pre :script :span :strong :style :textarea :ul}
  "A list of tags that need an explicit ending tag when rendered.")

(defn explicit-ending-tag?
  "Returns true if tag needs an explicit ending tag, even if the body of the
  tag is empty."
  [tag]
  (container-tags (keyword (str* tag))))

(defn html-tree
  "Turns a tree of vectors into a string of HTML. Any sequences in the
  tree are expanded out."
  [tree]
  (if (vector? tree)
    (let [[tag attrs & body] (ensure-attrs tree)
          [tag attrs]        (parse-css-tag tag attrs)
          body               (expand-seqs body)]
      (if (or body (explicit-ending-tag? tag))
        (create-tag tag attrs (apply html body))
        (create-closed-tag tag attrs)))
    (str tree)))

(defn html
  "Format trees of vectors into a string of HTML."
  [& trees]
  (map-str html-tree trees))
