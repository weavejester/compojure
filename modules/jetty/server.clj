(in-ns* 'jetty-server)
(require-module 'http)

(import '(org.mortbay.jetty Server)
        '(org.mortbay.jetty.servlet Context ServletHolder)
        '(org.mortbay.util.ajax ContinuationSupport))

;;;;; HTTP server ;;;;

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

;;;;; Asynchronous response (Comet) helpers ;;;;;

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

(defn async-response
  "A convenience function for creating a potentially delayed HTTP response via
  suspend-request. The predicate pred denotes whether to suspend the request,
  and the action denotes what to do after."
  ([request key pred action]
    (async-response request key pred action *default-timeout*))
  ([request key pred action timeout]
    (locking request
      (when-not (pred key)
        (suspend-request request key timeout)))
    (action key)))
      
(defn resume-requests
  "Resumes a group of suspended requests by key. See: suspend-request."
  [key]
  (let [cset (dosync
               (return (*continuations* key)
                 (alter *continuations* dissoc key)))]
    (doseq cc cset
      (. cc (resume)))))

(def resume-request resume-requests) ; An alias for resume-requests

; Bind the functions that need the request object, so we don't have to
; keep passing in the request object manually when in a resource.
(add-resource-binding
  'suspend-request
  `(partial suspend-request ~'request))

(add-resource-binding
  'async-response
  `(partial async-response ~'request))
