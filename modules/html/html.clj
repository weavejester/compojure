(in-ns* 'compojure-html)
(require "modules/html/xml.clj")
(refer 'compojure-xml)

; Currently XML and HTML are treated the same. In future they'll be some HTML
; specific logic for certain tags (e.g. Anything in a <script> tag will be
; wrapped with CDATA).
(defmacro html [sexpr]
  `(xml ~sexpr))

(defn include-javascript
  "Include external javascript tag"
  [& files]
  (str-map
    #(html
      (:script {
         :type "text/javascript"
         :src  (str "javascript/" % ".js")}))
    files))

(defmacro htmldoc
  [title & body]
  `(html
     (:html
       (:head
         (:title ~title)
       (:body
         ~@body)))))
