(load-file "plugins/html/xml.clj")
(refer 'compojure-xml)

(def html-tags
  '(a abbr acronym address area b base bdo big blockquote body br button
    caption cite code col colgroup dd del div dfn dl dt em fieldset form frame
    frameset h1 h2 h3 h4 h5 h6 head hr html i iframe img input ins kbd label
    legend li link noframes noscript object ol optgroup option p param pre q
    samp script select small span strong style sub sup table tbody td textarea
    tfoot th thead title tr tt ul var))

(defmacro h [& body]
  "Convert s-expressions into a string of HTML.
  e.g.
    (h (html (body (p \"Hello World\"))))"
  `(let
    ~(apply vector
       (mapcat
         (fn [t] `(~t (partial tag '~t)))
         html-tags))
    ~@body))
