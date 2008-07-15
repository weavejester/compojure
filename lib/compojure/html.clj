;; html.clj -- HTML generator library for Compojure

(clojure/in-ns 'html)
(clojure/refer 'clojure)

(lib/use compojure seq-utils)

(import '(clojure.lang Sequential))

(defn escape-html
  "Change special characters into HTML character entities."
  [string]
  (.. (str string)
    (replaceAll "&"  "&amp;")
    (replaceAll "<"  "&lt;")
    (replaceAll ">"  "&gt;")
    (replaceAll "\"" "&quot;")))

(def h escape-html)    ; Shortcut for escaping HTML

(defn- make-attrs
  "Turn a map into a string of XML attributes."
  [attrs]
  (str-map
    (fn [[key val]]
      (str* " " key "=\"" (h val) "\""))
    attrs))

(def xml)

(defn- make-tag
  "Wrap some content in an XML tag."
  [tag attrs content]
  (str* "<" tag (make-attrs attrs) ">"
          content
        "</" tag ">"))

(defn- make-closed-tag
  "Make a closed XML tag with no content."
  [tag attrs]
  (str* "<" tag (make-attrs attrs) " />"))

(defn- xml-map
  "Turn a list of XML into a string using a supplied formatter."
  [format trees]
  (str-map (partial xml format) trees))

(defn basic-xml-formatter
  "Format XML without any indentation or extra whitespace."
  [next-format tag attrs body]
  (if body
    (make-tag tag attrs (xml-map next-format body))
    (make-closed-tag tag attrs)))

(defn block-xml-formatter
  "Format XML with indentation and with no 'single' tags."
  [next-format tag attrs body]
  (str (if body
         (let [content (xml-map next-format body)]
           (make-tag tag attrs (str "\n" (indent content))))
         (make-tag tag attrs ""))
       "\n"))

(defn expand-seqs
  "Expand out all the sequences in a collection."
  [coll]
  (mapcat #(if (seq? %) % (list %)) coll))

(defn xml
  "Turns a tree of vectors into a string of XML. Any sequences in the
  tree are expanded out."
  ([tree]
    (xml basic-xml-formatter tree))
  ([format tree]
    (if (vector? tree)
      (let [tree (expand-seqs tree)]
        (if-let tag (first tree)
          (if (map? (second tree))
            (format format tag (second tree) (rrest tree))
            (format format tag {} (rest tree)))
          ""))
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

(defn html-formatter
  "Format HTML in a readable fashion."
  [next-format tag attrs body]
  (let [format  (if (contains? html-block-tags tag)
                  block-xml-formatter
                  basic-xml-formatter)
        content (format html-formatter tag attrs body)]
    (if (contains? html-line-tags tag)
      (str content "\n")
      content)))

(defn html
  "Nicely formats a tree of vectors into HTML."
  [& trees]
  (xml-map html-formatter trees))
