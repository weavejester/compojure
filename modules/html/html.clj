(compojure/module html)

(defn escape-html
  "Change special characters into HTML character entities."
  [string]
  (.. (str string)
    (replaceAll "&"  "&amp;")
    (replaceAll "<"  "&lt;")
    (replaceAll ">"  "&gt;")
    (replaceAll "\"" "&quot;")))

(defn- make-attrs
  "Turn a map into a string of XML attributes."
  [attrs]
  (str-map
    (fn [[k v]]
      (str* " " k "=\"" (escape-html v) "\""))
    attrs))

(def xml)

(defn- make-tag
  "Create an XML tag given a name, attribute map, and seq of contents."
  [name attrs contents]
  (if contents
    (str* "<" name (make-attrs attrs) ">"
          (str-map xml contents)
          "</" name ">")
    (str* "<" name (make-attrs attrs) " />")))

(defn xml
  "Turn a tree of vectors into a string of XML."
  [tree]
  (if (vector? tree)
    (if-let tag (first tree)
      (if (map? (second tree))
        (make-tag (str* tag) (second tree) (rrest tree))
        (make-tag (str* tag) {} (rest tree)))
      "")
    (str tree)))
