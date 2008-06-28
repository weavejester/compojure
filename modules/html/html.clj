(in-ns* 'compojure-html)

(defn- quote-str
  "Change special characters into HTML character entities."
  [s]
  (.. (str s) (replaceAll "\"" "&quot;")
              (replaceAll "<"  "&lt;")
              (replaceAll ">"  "&gt;")
              (replaceAll "&"  "&amp;")))

(defn- make-attrs
  "Turn a map into a string of XML attributes."
  [attrs]
  (str-map
    (fn [[k v]]
      (str* " " k "=\"" (quote-str v) "\""))
    attrs))

(defn- make-tag
  "Create an XML tag given a name, attribute map, and seq of contents."
  [name attrs contents]
  (if contents
    (str* "<" name (make-attrs attrs) ">\n"
          (indent (apply str contents))
          "</" name ">\n")
    (str* "<" name (make-attrs attrs) " />\n")))

(defn tag
  "Generate an XML tag.
  e.g.
    (tag :em \"text\")
    (tag 'a :href \"#top\" \"Back to top\")"
  [name & contents]
  (let [[attrs contents] (kwargs contents)]
    (make-tag name attrs contents)))

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

; Currently XML and HTML are treated the same.
(defmacro html [sexpr]
  `(xml ~sexpr))

(defn javascript-tag
  "A javascript HTML tag."
  [script]
  (tag :script :type "text/javascript" script))

(defn javascript-sources
  "Include external javascript sources."
  [sources]
  (str-map
    #(tag :script :type "text/javascript" :src (str %))
    sources))

(defmacro htmldoc
  [options & body]
  (let [[options body] (kwargs body)]
   `(html
      (:html
        (:head
          (:title ~(options :title))
          (javascript-sources
            '~(map #(str "/javascript/" % ".js") 
                    (options :javascript))))
        (:body
          ~@body)))))
