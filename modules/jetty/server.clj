(in-ns* 'jetty-server)
(require-module 'http)

(import '(org.mortbay.jetty Server)
        '(org.mortbay.jetty.servlet Context ServletHolder)
        '(org.mortbay.util.ajax ContinuationSupport))

(def *default-timeout* 300000)   ; 5 minute timeout

(def *continuations*
  (ref {}))

(defn get-continuation
  "Get the current Jetty continuation object for the request."
  [request]
  (. ContinuationSupport (getContinuation request request)))

(defn suspend-request
  "Use Jetty's pseudo-continuation support to suspend handling a HTTP request.
  When called, the current servlet ends and is re-called when the request is
  resumed. When resumed, suspend-request exits immediatedly.
    Requests are stored in a hash with a user defined key, so you can resume
  a group of requests at once (e.g. all the requests in one chat room)."
  ([request key]
    (suspend-request request key *default-timeout*))
  ([request key timeout]
    (let [cc (get-continuation request)]
      (dosync
        (let [cset    (*continuations* key)
              cset+cc (conj (set cset) cc)]
          (alter *continuations* assoc key cset+cc)))
      (. cc (suspend timeout)))))

(defn resume-requests
  "Resumes a group of suspended requests by key. See: suspend-request."
  [key]
  (let [cset (dosync (return (*continuations* key)
                       (alter *continuations* dissoc key)))]
    (doseq cc cset
      (. cc (resume)))))

(def resume-request resume-requests) ; An alias for resume-requests

; Bind suspend-request to all resource calls, so you don't have to manually
; specify the request object each time.
(add-resource-binding
  'suspend-request
  `(partial suspend-request ~'request))

(defn http-server
  "Create a new Jetty HTTP server with the supplied servlet."
  [servlet & options]
  (let [options (apply hash-map options)
        port    (or (options :port) 8080)
        server  (new Server port)
        context (new Context server "/" (. Context SESSIONS))
        holder  (new ServletHolder servlet)]
    (. context (addServlet holder "/*"))
    server))
