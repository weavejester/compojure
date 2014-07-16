(ns compojure.core
  "A DSL for building Ring handlers from smaller routes.

  Compojure routes are semantically the same as Ring handlers, with the
  exception that routes may return nil to indicate they do not match.

  This namespace provides functions and macros for concisely constructing
  routes and combining them together to form more complex functions."
  (:require [clojure.string :as str])
  (:use clout.core
        compojure.response
        [clojure.tools.macro :only (name-with-attributes)]))

(defn- method-matches?
  "True if this request matches the supplied request method."
  [method request]
  (let [request-method (request :request-method)
        form-method    (get-in request [:form-params "_method"])]
    (if (and form-method (= request-method :post))
      (= (str/upper-case (name method))
         (str/upper-case form-method))
      (= method request-method))))

(defn- if-method
  "Evaluate the handler if the request method matches."
  [method handler]
  (fn [request]
    (cond
      (or (nil? method) (method-matches? method request))
        (handler request)
      (and (= :get method) (= :head (:request-method request)))
        (if-let [response (handler request)]
          (assoc response :body nil)))))

(defn- assoc-route-params
  "Associate route parameters with the request map."
  [request params]
  (merge-with merge request {:route-params params, :params params}))

(defn- if-route
  "Evaluate the handler if the route matches the request."
  [route handler]
  (fn [request]
    (if-let [params (route-matches route request)]
      (handler (assoc-route-params request params)))))

(defn- prepare-route
  "Pre-compile the route."
  [route]
  (cond
    (string? route)
      `(route-compile ~route)
    (vector? route)
      `(route-compile
        ~(first route)
        ~(apply hash-map (rest route)))
    :else
      `(if (string? ~route)
         (route-compile ~route)
         ~route)))

(defn- assoc-&-binding [binds req sym]
  (assoc binds sym `(dissoc (:params ~req)
                            ~@(map keyword (keys binds))
                            ~@(map str (keys binds)))))

(defn- assoc-symbol-binding [binds req sym]
  (assoc binds sym `(get-in ~req [:params ~(keyword sym)]
                      (get-in ~req [:params ~(str sym)]))))

(defn- vector-bindings
  "Create the bindings for a vector of parameters."
  [args req]
  (loop [args args, binds {}]
    (if-let [sym (first args)]
      (cond
        (= '& sym)
          (recur (nnext args) (assoc-&-binding binds req (second args)))
        (= :as sym)
          (recur (nnext args) (assoc binds (second args) req))
        (symbol? sym)
          (recur (next args) (assoc-symbol-binding binds req sym))
        :else
          (throw (Exception. (str "Unexpected binding: " sym))))
      (mapcat identity binds))))

(defmacro let-request [[bindings request] & body]
  (if (vector? bindings)
    `(let [~@(vector-bindings bindings request)] ~@body)
    `(let [~bindings ~request] ~@body)))

(defn make-route
  "Returns a function that will only call the handler if the method and Clout
  route match the request."
  [method route handler]
  (if-method method
    (if-route route
      (fn [request]
        (render (handler request) request)))))

(defn compile-route
  "Compile a route in the form (method path & body) into a function."
  [method route bindings body]
  `(make-route
    ~method ~(prepare-route route)
    (fn [request#]
      (let-request [~bindings request#] ~@body))))

(defn routing
  "Apply a list of routes to a Ring request map."
  [request & handlers]
  (some #(% request) handlers))

(defn routes
  "Create a Ring handler by combining several handlers into one."
  [& handlers]
  #(apply routing % handlers))

(defmacro defroutes
  "Define a Ring handler function from a sequence of routes. The name may
  optionally be followed by a doc-string and metadata map."
  [name & routes]
  (let [[name routes] (name-with-attributes name routes)]
   `(def ~name (routes ~@routes))))

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

(defmacro OPTIONS "Generate an OPTIONS route."
  [path args & body]
  (compile-route :options path args body))

(defmacro PATCH "Generate a PATCH route."
  [path args & body]
  (compile-route :patch path args body))

(defmacro ANY "Generate a route that matches any method."
  [path args & body]
  (compile-route nil path args body))

(defn- remove-suffix [path suffix]
  (subs path 0 (- (count path) (count suffix))))

(defn- path-normalize [path]
  (path-encode (path-decode path)))

(defn- wrap-context [handler]
  (fn [request]
    (let [uri     (path-normalize (:uri request))
          path    (:path-info request uri)
          context (or (:context request) "")
          subpath (-> request :route-params :__path-info path-encode)]
      (handler
       (-> request
           (update-in [:params] dissoc :__path-info)
           (update-in [:route-params] dissoc :__path-info)
           (assoc :uri       uri
                  :path-info (if (= subpath "") "/" subpath)
                  :context   (remove-suffix uri subpath)))))))

(defn- context-route [route]
  (let [re-context {:__path-info #"|/.*"}]
    (cond
      (string? route)
       `(route-compile ~(str route ":__path-info") ~re-context)
      (vector? route)
       `(route-compile
         ~(str (first route) ":__path-info")
         ~(merge (apply hash-map (rest route)) re-context))
      :else
       `(route-compile (str ~route ":__path-info") ~re-context))))

(defmacro context
  "Give all routes in the form a common path prefix and set of bindings.

  The following example demonstrates defining two routes with a common
  path prefix ('/user/:id') and a common binding ('id'):

    (context \"/user/:id\" [id]
      (GET \"/profile\" [] ...)
      (GET \"/settings\" [] ...))"
  [path args & routes]
  `(#'if-route ~(context-route path)
     (#'wrap-context
       (fn [request#]
         (let-request [~args request#]
           (routing request# ~@routes))))))

(defmacro let-routes
  "Takes a vector of bindings and a body of routes. Equivalent to:
  (let [...] (routes ...))"
  [bindings & body]
  `(let ~bindings (routes ~@body)))
