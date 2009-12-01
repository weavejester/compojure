;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.routes
  "Macros and functions for compiling routes in the form (method path & body)
   into stand-alone functions that return the return value of the body, or the
   keyword :next if they don't match."
  (:use clout)
  (:use compojure.control)
  (:use compojure.http.response)
  (:use compojure.http.request))

(defn method-matches
  "True if this request matches the supplied method."
  [method request]
  (let [request-method (request :request-method)
        form-method    (-> request :form-params :_method)]
    (or (nil? method)
        (if (and form-method (= request-method :post))
          (= (.toUpperCase (name method)) form-method)
          (= method request-method)))))

(defn- request-matcher
  "Compiles a function to match a HTTP request against the supplied method
  and route."
  [method route]
  (let [matcher (if (string? route)
                  (route-compile route)
                  route)]
   `(fn [request#]
      (and (method-matches ~method request#)
           (route-matches ~route request#)))))

(defmacro with-request-bindings
  "Add shortcut bindings for the keys in a request map."
  [request & body]
  `(let [~'request ~request
         ~'params  (:params  ~'request)
         ~'cookies (:cookies ~'request)
         ~'session (:session ~'request)
         ~'flash   (:flash   ~'request)]
     ~@body))

(defn assoc-route-params
  "Associate route parameters with the request map."
  [request params]
  (-> request
    (assoc :route-params params)
    (assoc :params (merge (:params request)
                          (if (map? params) params)))))

(defn compile-route
  "Compile a route in the form (method path & body) into a function."
  [method path body]
  `(let [matcher# ~(request-matcher method path)]
     (fn [request#]
       (if-let [route-params# (matcher# request#)]
         (let [request# (assoc-route-params request# route-params#)]
           (create-response request#
             (with-request-bindings request# ~@body)))))))

(defn routes*
  "Create a Ring handler by combining several handlers into one."
  [& handlers]
  (fn [request]
    (some #(% request) handlers)))

(defn routes
  "Create a Ring handler by combining several routes into one. Adds parameters
  and cookies to the request."
  [& handlers]
  (-> (apply routes* handlers)
    with-request-params
    with-cookies))

;; Macros for easily creating a compiled routing table

(defmacro defroutes
  "Define a Ring handler function from a sequence of routes. Takes an optional
  doc-string."
  [name doc? & routes]
  (let [[name & routes] (apply-doc name doc? routes)]
   `(def ~name
      (routes ~@routes))))

(defmacro GET "Generate a GET route."
  [path & body]
  (compile-route :get path body))

(defmacro POST "Generate a POST route."
  [path & body]
  (compile-route :post path body))

(defmacro PUT "Generate a PUT route."
  [path & body]
  (compile-route :put path body))

(defmacro DELETE "Generate a DELETE route."
  [path & body]
  (compile-route :delete path body))

(defmacro HEAD "Generate a HEAD route."
  [path & body]
  (compile-route :head path body))

(defmacro ANY "Generate a route that matches any method."
  [path & body]
  (compile-route nil path body))
