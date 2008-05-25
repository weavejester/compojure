(in-ns 'compojure-xml)
(clojure/refer 'clojure)
(clojure/refer 'compojure)

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
  (apply str
    (map
      (fn [[k v]]
        (str " " (name-str k) "=\"" (quote-special v) "\""))
      attrs)))

(defn- make-tag
  "Create an XML tag given a name, attribute map, and seq of contents."
  [name attrs & contents]
  (str "<" name (make-attrs attrs) ">"
       (apply str contents)
       "</" name ">"))

(defn tag
  "Generate an XML tag.
  e.g.
    (tag 'em \"text\")
    (tag 'a {:href \"#top\"} \"Back to top\")"
  [name & contents]
  (if (map? (first contents))
    (apply make-tag name contents)
    (apply make-tag name {} contents)))
