(require "modules/html/xml.clj")
(refer 'compojure-xml)

; Currently XML and HTML are treated the same. In future they'll be some HTML
; specific logic for certain tags (e.g. Anything in a <script> tag will be
; wrapped with CDATA).
(defmacro html [x]
  `(xml ~x))
