;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.core
  "A concise syntax for generating Ring handlers."
  (:use [clout :only (route-matches route-compile)]
        [compojure.response :only (render)]))

(defn- method-matches
  "True if this request matches the supplied method."
  [method request]
  (let [request-method (request :request-method)
        form-method    (-> request :form-params :_method)]
    (or (nil? method)
        (if (and form-method (= request-method :post))
          (= (.toUpperCase (name method)) form-method)
          (= method request-method)))))

(defn- prepare-route
  "Pre-compile the route."
  [route]
  (cond
    (string? route)
      (route-compile route)
    (seq? route)
      (route-compile (first route) (apply hash-map (rest route)))
    :else route))

(defn- assoc-route-params
  "Associate route parameters with the request map."
  [request params]
  (merge-with merge request {:route-params params, :params params}))

(defn- param-vector-bindings
  "Create the bindings for a vector of parameters."
  [request bindings body]
  (let [[args [_ more]] (split-with #(not= % '&) bindings)]
    `(let [{:keys ~(vec args)} (~request :params)
          ~@(if more [more `(dissoc (~request :params) ~@(map keyword args))])]
       ~@body)))

(defmacro bind-request
  "Bind a request to a collection of symbols. The collection can be a Clojure
  map destructuring binding for the request map, or it can be a vector of
  parameter bindings."
  [request bindings & body]
  (if (vector? bindings)
    (param-vector-bindings request bindings body)
    `(let [~bindings ~request] ~@body)))

(defn- compile-route
  "Compile a route in the form (method path & body) into a function."
  [method route bindings body]
  `(let [route# ~(prepare-route route)]
     (fn [request#]
       (if (#'method-matches ~method request#)
         (if-let [route-params# (route-matches route# request#)]
           (let [request# (#'assoc-route-params request# route-params#)]
             (bind-request request# ~bindings
               (render (do ~@body)))))))))

(defn routes
  "Create a Ring handler by combining several handlers into one."
  [& handlers]
  (fn [request]
    (some #(% request) handlers)))

(defn- apply-doc
  "Return a symbol and body with an optional docstring applied."
  [name doc? body]
  (if (string? doc?)
    (list* (vary-meta name assoc :doc doc?) body)
    (list* name doc? body)))

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
