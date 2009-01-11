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
;;           (domap [letter '(a b c d)]
;;             [:li letter])]]])

(ns compojure.html
  (:use (compojure str-utils)
        (clojure.contrib def
                         seq-utils
                         str-utils)))

(defn- optional-attrs
  "Adds an optional attribute map to the supplied function's arguments."
  [func]
  (fn [attrs & body]
    (if (map? attrs)
      (let [[tag func-attrs & body] (apply func body)]
        (apply vector tag (merge func-attrs attrs) body))
      (apply func attrs body))))

(load "html/form_functions")
(load "html/page_functions")

(defn escape-html
  "Change special characters into HTML character entities."
  [string]
  (.. (str string)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(def h escape-html)    ; Shortcut for escaping HTML

(defn- map-to-attrs
  "Turn a map into a string of XML attributes, sorted by attribute name."
  [attrs]
  (str-map
    (fn [[key val]]
      (if key
        (str " " key "=\"" (h val) "\"")))
    (sort
      (map (fn [[key val]]
             (cond
               (true? val) [(str* key) (str* key)]
               (not val)   [nil nil]
               :otherwise  [(str* key) (str* val)]))
           attrs))))

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

(def css-lexer #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

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

(declare xml)

(defn xml-tree
  "Turns a tree of vectors into a string of XML. Any sequences in the
  tree are expanded out."
  [tree]
  (if (vector? tree)
    (let [[tag attrs & body] (ensure-attrs tree)
          [tag attrs]        (parse-css-tag tag attrs)
          body               (expand-seqs body)]
      (if body
        (create-tag tag attrs (apply xml body))
        (create-closed-tag tag attrs)))
    (str tree)))

(defn xml
  "Format trees of vectors into a string of XML."
  [& trees]
  (str-map xml-tree trees))

(def html xml)
