(refer 'lib)
(require http/http)
(refer 'http)

; Use the resources defined using the http module as the root servlet
(def *servlet*
  resource-servlet)
