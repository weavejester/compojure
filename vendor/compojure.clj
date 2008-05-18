(load-file "vendor/resource.clj")

(in-ns 'compojure)
(clojure/refer 'clojure)

(def #^{:doc 
  "A servlet that handles all requests into Compojure. Suitable for
  integrating with the web server of your choice."}
  compojure-servlet
    (proxy [HttpServlet] []
      (service [request response]
        (let [context  (. this (getServletContext))
              resource (find-resource request response)]
          (resource context request response)))))
