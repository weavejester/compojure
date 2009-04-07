;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.server.grizzly
;; 
;; Clojure interface to start an embedded Grizzly server.
;;
;; For now, grizzly support is not compiled or included by default
;; when you require or use compojure.  To try grizzly:
;;
;; 1. Download the latest grizzly-servlet-webserver binary
;;    from the http://grizzly.dev.java.net.  As of now:
;;    http://download.java.net/maven/2/com/sun/grizzly/grizzly-servlet-webserver/1.9.10/grizzly-servlet-webserver-1.9.10.jar
;; 2. Place the above file in deps.
;; 3. Run ant:
;;    ant -Dwith.grizzly
;; 4. Put the new compojure.jar in your classpath.
;;    
;; You must require it with an alias or it will conflict with jetty.
;; Use it the same way you used jetty.
;;
;; (require ['compojure.server.grizzly :as 'grizzly])
;; (grizzly/run-server {:port 8081} "/*" (servlet my-app))
;; 
;; Jetty doesn't set any headers by default, so browsers assume
;; text/html.  Grizzly defaults to text/plain so your route will need
;; {:headers {"Content-Type" "text/html"}}.
;;
;; (defroutes my-app
;;   (GET "/grizzly"
;;     [{:headers {"Content-Type" "text/html"}}
;;     "<html><body><h1>Hello Grizzly.</h1></body></html>"])
;;   (GET "/*"
;;     (or (serve-file (params :*)) :next))
;;   (ANY "*"
;;     (page-not-found)))

(ns compojure.server.grizzly
  (:use compojure.control)
  (:use compojure.server.common)
  (:import com.sun.grizzly.http.embed.GrizzlyWebServer)
  (:import com.sun.grizzly.http.servlet.ServletAdapter))

(defn servlet-adapter
  "Wrap a servlet in a ServletAdapter object with a supplied set of parameters
  to be set on servlet init."
  [servlet & params]
  (let [adapter (new ServletAdapter servlet)
        params (partition 2 params)]
    (doseq [[key val] params]
      (.setInitParameter adapter (name key) (str val)))
    adapter))

(defn add-servlet!
  "Add a servlet to a Grizzly server. Servlets can be connected to a relative
  path or an absolute URL.  Unlike the Jetty server, no Virtual Hosts
  are setup."
  [server url-or-path servlet]
  (let [[host path] (get-host-and-path url-or-path)
        adapter      (if (instance? ServletAdapter servlet)
                       servlet
                       (ServletAdapter. servlet))]
    (.addGrizzlyAdapter server adapter (into-array [path]))))

(defn- create-server
  "Construct a Grizzly Server instance."
  [options servlets]
  (let [port     (or (options :port) 8080)
        server   (GrizzlyWebServer. port)
        servlets (partition 2 servlets)]
    (doseq [[url-or-path servlet] servlets]
      (add-servlet! server url-or-path servlet))
    server))

(defn grizzly-server
  "Create a new Grizzly HTTP server with the supplied options and servlets."
  [options & servlets]
  (apply-optional-map create-server options servlets))

(defmacro defserver
  "Shortcut for (def name (http-server args))"
  [name & args]
  `(def ~name (grizzly-server ~@args)))

(defn start "Start a HTTP server."
  [server]
  (.start server))

(defn stop  "Stop a HTTP server."
  [server]
  (.stop server))

(defn run-server
  "Create and start a new Grizzly HTTP server."
  [& server-args]
  (.start (apply grizzly-server server-args)))
