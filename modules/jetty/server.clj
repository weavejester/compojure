(in-ns* 'jetty-server)
(require-module 'http)

(import '(org.mortbay.jetty Server)
        '(org.mortbay.jetty.servlet Context ServletHolder)
        '(org.mortbay.util.ajax ContinuationSupport))

(defn continuation
  ([request] (continuation nil))
  ([request mutex]
    (let [cc (. ContinuationSupport (getContinuation request mutex))]
      (.. cc (suspend)))))

;(add-resource-binding
;  'continuation
;  `(partial continuation ~'request))

(defn http-server
  [servlet & options]
  (let [options (apply hash-map options)
        port    (or (options :port) 8080)
        server  (new Server port)
        context (new Context server "/" (. Context SESSIONS))
        holder  (new ServletHolder servlet)]
    (. context (addServlet holder "/*"))
    server))
