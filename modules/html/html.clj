(in-ns* 'compojure-html)
(require "modules/html/xml.clj")
(refer 'compojure-xml)

; Currently XML and HTML are treated the same. In future they'll be some HTML
; specific logic for certain tags (e.g. Anything in a <script> tag will be
; wrapped with CDATA).
(defmacro html [sexpr]
  `(xml ~sexpr))

(defn javascript-tag
  "A javascript HTML tag."
  [script]
  (tag :script {:type "text/javascript"} script))

(defn javascript-sources
  "Include external javascript sources."
  [sources]
  (str-map
    #(tag :script {:type "text/javascript" :src (str %)})
    sources))

(defmacro htmldoc
  [options & body]
  `(html
     (:html
       (:head
         (:title ~(options :title))
         (javascript-sources
           '~(map #(str "/javascript/" % ".js") 
                   (options :javascript))))
       (:body
         ~@body))))
