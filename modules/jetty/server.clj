(in-ns* 'jetty-server)
(require-module 'http)

(import '(org.mortbay.jetty Server)
        '(org.mortbay.jetty.servlet Context ServletHolder)
        '(org.mortbay.util.ajax ContinuationSupport))

(def *default-timeout* 30000)   ; 5 minute timeout

(defn suspend-request
  ([request]
    (suspend-request *default-timeout*))
  ([request timeout]
    (.
      (. ContinuationSupport (getContinuation request request))
      (suspend timeout))))

(add-resource-binding
  'suspend-request
  `(partial suspend-request ~'request))

(defn http-server
  [servlet & options]
  (let [options (apply hash-map options)
        port    (or (options :port) 8080)
        server  (new Server port)
        context (new Context server "/" (. Context SESSIONS))
        holder  (new ServletHolder servlet)]
    (. context (addServlet holder "/*"))
    server))
