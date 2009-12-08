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

(defn- assoc-binding
  "Associate an argument with a map of bindings."
  [bindings arg]
  (assoc bindings (symbol (name arg)) (keyword (str arg))))

(defn- single-bindings
  "Create a binding map of all the single value arguments."
  [args]
  (reduce assoc-binding {} (take-while #(not= % '&) args)))

(defn- assoc-rest-bindings
  "Assoc the '& rest' argument to a binding map."
  [bindings args]
  (if-let [rest-arg (second (drop-while #(not= % '&) args))]
    (assoc bindings :as rest-arg)
    bindings))
    
(defn- make-param-bindings
  "Return a map of parameter bindings derived from the request map and a
  vector of argument names."
  [args]
  (-> (single-bindings args)
      (assoc-rest-bindings args)))

(defn- prepare-route
  "Pre-compile the route."
  [route]
  (cond
    (string? route)
      (route-compile route)
    (seq? route)
      (route-compile (first route) (apply hash-map (rest route)))
    :else
      route))

(defn assoc-route-params
  "Associate route parameters with the request map."
  [request params]
  (merge-with merge request {:route-params params, :params params}))

(defn compile-route
  "Compile a route in the form (method path & body) into a function."
  [method route args body]
  (let [bindings (make-param-bindings args)]
    `(let [route# ~(prepare-route route)]
       (fn [request#]
         (if (method-matches ~method request#)
           (if-let [route-params# (route-matches route# request#)]
             (let [request#  (assoc-route-params request# route-params#)
                   ~bindings (request# :params)]
               (create-response request# (do ~@body)))))))))

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
  [path args & body]
  (compile-route :get path args body))

(defmacro POST "Generate a POST route."
  [path args & body]
  (compile-route :post path args body))

(defmacro PUT "Generate a PUT route."
  [path args & body]
  (compile-route :put path args body))

(defmacro DELETE "Generate a DELETE route."
  [path args & body]
  (compile-route :delete path args body))

(defmacro HEAD "Generate a HEAD route."
  [path args & body]
  (compile-route :head path args body))

(defmacro ANY "Generate a route that matches any method."
  [path args & body]
  (compile-route nil path args body))
