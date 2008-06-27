(in-ns 'compojure-xml)
(clojure/refer 'clojure)
(clojure/refer 'compojure)

(defn- indent
  "Indent a string of text."
  [s]
  (str-map
    #(str "  " % "\n")
    (re-split #"\n" s)))

(defn- quote-special
  "Change special characters into HTML character entities."
  [s]
  (.. s (replaceAll "\"" "&quot;")
        (replaceAll "<"  "&lt;")
        (replaceAll ">"  "&gt;")
        (replaceAll "&"  "&amp;")))

(defn- make-attrs
  "Turn a map into a string of XML attributes."
  [attrs]
  (str-map
    (fn [[k v]]
      (str* " " k "=\"" (quote-special v) "\""))
    attrs))

(defn- make-tag
  "Create an XML tag given a name, attribute map, and seq of contents."
  [name attrs & contents]
  (str* "<" name (make-attrs attrs) ">\n"
        (indent (apply str contents))
        "</" name ">\n"))

(defn tag
  "Generate an XML tag.
  e.g.
    (tag :em \"text\")
    (tag 'a {:href \"#top\"} \"Back to top\")"
  [name & contents]
  (if (map? (first contents))
    (apply make-tag name contents)
    (apply make-tag name {} contents)))

(defmacro xml
  "Any forms starting with a keyword in the body of this macro get an implicit
  tag function.
  e.g.
    (xml (:body (:p \"Hello World\")))"
  [body]
  (if (keyword? (first body))
    (list* 'tag (first body)
      (map
        #(if (seq? %) (list 'xml %) %)
        (rest body)))
    body))
