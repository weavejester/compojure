(in-ns* 'jetty-server)
(require-module 'http)

(import '(org.mortbay.jetty Server)
        '(org.mortbay.jetty.servlet Context ServletHolder)
        '(org.mortbay.util.ajax ContinuationSupport))

(def *default-timeout* 300000)   ; 5 minute timeout

(def *continuations*
  (ref {}))

(defn get-continuation
  [request]
  (. ContinuationSupport (getContinuation request request)))

(defn suspend-request
  ([request key]
    (suspend-request request key *default-timeout*))
  ([request key timeout]
    (let [cc (get-continuation request)]
      (dosync
        (let [cset (*continuations* key)]
          (alter *continuations*
            #(assoc % key (set (conj cset cc))))))
      (. cc (suspend timeout)))))

(defn resume-request
  [key]
  (doseq continuation
    (dosync (return (*continuations* key)
              (alter *continuations* #(dissoc % key))))
    (. continuation (resume))))

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
