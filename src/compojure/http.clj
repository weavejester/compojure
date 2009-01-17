;; compojure.http
;;
;; Compojure library for constructing HTTP servlet proxy objects. 
;; 
;; Here's a small taste of the syntax:
;;
;;   (defservlet example
;;     "An example servlet that contains a bit of everything."
;;     (GET "/"
;;       "Hello World")
;;     (GET "/:name"
;;       (str "Hello " (route :name)))
;;     (GET "/image"
;;       (file "public/image.png"))
;;     (GET "/error"
;;       [500 "Error 500"])
;;     (POST "/mesg"
;;       (redirect-to "/"))
;;     (GET "/header"
;;       [{"X-Fortune" "Be prepared!"}
;;        "Custom X-Header"])
;;     (PUT "/:var"
;;       (dosync
;;         (commute session assoc :var (route :var)))
;;     (ANY "/*"
;;       (page-not-found))

(ns compojure.http
  (:use (compojure control
                   file-utils
                   parser
                   str-utils))
  (:use compojure.http.routes)
  (:use compojure.http.servlet)
  (:import (java.io File
                    InputStream
                    FileInputStream)
           (java.net URL)
           (java.util Enumeration
                      Map$Entry)
           (java.util.regex Pattern)
           (javax.servlet ServletContext)
           (javax.servlet.http Cookie
                               HttpServlet
                               HttpServletRequest
                               HttpServletResponse)))
;;;; Mimetypes ;;;;
 
(defn context-mimetype
  "Guess the mimetype of a filename. Defaults to 'application/octet-stream'
  if the mimetype is unknown."
  [#^ServletContext context filename]
  (or (.getMimeType context filename)
      "application/octet-stream"))

;;;; Handler functions ;;;;
 
(defstruct http-handler
  :method
  :route
  :function)

(defmacro handler-fn
  "Macro that wraps the body of a handler up in a standalone function."
  [& body]
  `(fn ~'[route context request response]
     (let ~'[method    (.getMethod    request)
             full-path (.getPathInfo  request)
             params    (compojure.http/get-params  request)
             headers   (compojure.http/get-headers request)
             mimetype #(compojure.http/context-mimetype context (str %))
             session   (compojure.http/get-session request)
             cookies   (compojure.http/get-cookies request)]
       (do ~@body))))
 
(defn- apply-http-handler
  "Finds and evaluates the handler that matches the HttpServletRequest. If the
  handler returns :next, the next matching handler is evaluated."
  [handlers context request response]
  (let [method   (get-method request)
        path     (.getPathInfo request)
        method= #(or (nil? %) (= method %))
        route?   (fn [handler]
                   (if (method= (handler :method))
                     (match-route (handler :route) path)))
        response? (fn [handler]
                    (if-let [route-params (route? handler)]
                      (let [func (handler :function)
                            resp (func route-params
                                       context
                                       request
                                       response)]
                        (if (not= :next resp)
                          (or resp [])))))]
    (update-response response context
      (some response? handlers))))

;;;; Public macros ;;;;


;;;; Helper functions ;;;;

(defn new-cookie
  "Create a new Cookie object."
  [name value & attrs]
  (let [cookie (new Cookie (str* name) value)
        attrs  (apply hash-map attrs)
        iff    (fn [attr func] (if attr (func attr)))]
    (iff (attrs :comment) #(.setComment cookie %))
    (iff (attrs :domain)  #(.setDomain  cookie %))
    (iff (attrs :max-age) #(.setMaxAge  cookie %))
    (iff (attrs :path)    #(.setPath    cookie %))
    (iff (attrs :secure)  #(.setSecure  cookie %))
    (iff (attrs :version) #(.setVersion cookie %))
    cookie))
 
(defn redirect-to
  "A shortcut for a '302 Moved' HTTP redirect."
  [location]
  [302 {"Location" location}])
 
(defn page-not-found
  "A shortcut to create a '404 Not Found' HTTP response."
  ([]         (page-not-found "public/404.html"))
  ([filename] [404 (file filename)]))
 
(defn- find-index-file
  "Search the directory for index.*"
  [dir]
  (first (filter
          #(re-matches #"index\\..*" (.toLowerCase (.getName %)))
           (.listFiles dir))))

(defn serve-file
  "Attempts to serve up a static file from a directory, which defaults to
  './public'. Nil is returned if the file does not exist. If the file is a
  directory, the function looks for a file in the directory called 'index.*'."
  ([path]
    (serve-file "public" path))
  ([root path]
    (let [filepath (file root path)]
      (cond
        (.isFile filepath)
          filepath
        (.isDirectory filepath)
          (find-index-file filepath)))))
 
;;;; Servlet creation ;;;;
 
(defn http-service
  "Represents the service method called by a HttpServlet."
  [#^HttpServlet this request response handlers]
  (.setCharacterEncoding response "UTF-8")
  (apply-http-handler handlers
                      (.getServletContext this)
                      request
                      response))
(defn servlet
  "Create a servlet from a sequence of handlers."
  [& handlers]
  (proxy [HttpServlet] []
    (service [request response]
      (http-service this request response handlers))))

(defn update-servlet
  "Update an existing servlet proxy with a new set of handlers."
  [object & handlers]
  (update-proxy object
    {"service" (fn [this request response]
                 (http-service this request response handlers))}))

(defmacro defservlet
  "Defines a new servlet with an optional doc-string, or if a servlet is
  already defined, it updates the existing servlet with the supplied handlers.
  Note that updating is not a thread-safe operation."
  [name doc & handlers]
  (if (string? doc)
    `(do (defonce
          ~(with-meta name (assoc (meta name) :doc doc))
           (proxy [HttpServlet] []))
         (update-servlet ~name ~@handlers))
    `(do (defonce ~name
           (proxy [HttpServlet] []))
         (update-servlet ~name ~doc ~@handlers))))

(defmacro defservice
  "Defines a service method with an optional prefix suitable for being used by
  genclass to compile a HttpServlet class.
  e.g. (defservice
         (GET \"/\" \"Hello World\"))
       (defservice \"myprefix-\"
         (GET \"/\" \"Hello World\"))"
  [prefix & handlers]
  (let [[prefix handlers] (if (string? prefix)
                           [prefix handlers]
                           ["-"   (cons prefix handlers)])]
    `(defn ~(symbol (str prefix "service"))
     ~'[this request response]
       (http-service ~'this ~'request ~'response (list ~@handlers)))))
