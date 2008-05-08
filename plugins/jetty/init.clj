(import '(org.mortbay.jetty Server)
        '(org.mortbay.jetty.handler AbstractHandler)
        '(javax.servlet.http HttpServletRequest HttpServletResponse))

(def handler
  (proxy [AbstractHandler] []
    (handle [target request response dispatch]
      (let [servlet (resource-servlet (find-resource request))]
        (if servlet (servlet request response)))
      (. request (setHandled true)))))

(def server (new Server 8080))
(. server (setHandler handler))
