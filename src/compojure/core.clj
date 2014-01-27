(ns compojure.core
  "A concise syntax for generating Ring handlers."
  (:require [clojure.string :as str])
  (:use clout.core
        compojure.response
        [clojure.core.incubator :only (-?>)]
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
        (-?> (handler request)
             (assoc :body nil)))))

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

(defn extract-parameters
  "Extract parameters from head of the sequence. Returns a vector with
   two elements: parameters and the rest of the body. Parameters can be
   in a form:
     1) a map (if followed by any form) [{:a 1 :b 2} :body] => [{:a 1 :b 2} :body]
     2) number of keywords & values [:a 1 :b 2 :body]       => [{:a 1 :b 2} :body]
     3) no parameters [:body]                               => [{} :body]
   Returns a tuple with parameters and body without the parameters"
  [c]
  {:pre [(sequential? c)]}
  (cond
    (and (map? (first c)) (> (count c) 1)) [(first c) (rest c)]
    (keyword? (first c))  (let [parameters (->> c
                                             (partition 2)
                                             (take-while (comp keyword? first))
                                             (mapcat identity)
                                             (apply hash-map))
                                form       (drop (* 2 (count parameters)) c)]
                            [parameters form])
    :else  [{} c]))

(defn- compile-route
  "Compile a route in the form (method path & body) into a function.
   Extracts optional meta-data from head of the body as defined in the
   extract-parameters -function."
  [method route bindings body]
  (let [[meta-data body] (extract-parameters body)]
    (with-meta
      `(make-route
         ~method ~(prepare-route route)
         (fn [request#]
           (let-request [~bindings request#] ~@body)))
      meta-data)))

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
  (let [f (compile-route :get path args body)]
    `(with-meta ~f ~(meta f))))

(defmacro POST "Generate a POST route."
  [path args & body]
  (let [f (compile-route :post path args body)]
    `(with-meta ~f ~(meta f))))

(defmacro PUT "Generate a PUT route."
  [path args & body]
  (let [f (compile-route :put path args body)]
    `(with-meta ~f ~(meta f))))

(defmacro DELETE "Generate a DELETE route."
  [path args & body]
  (let [f (compile-route :delete path args body)]
    `(with-meta ~f ~(meta f))))

(defmacro HEAD "Generate a HEAD route."
  [path args & body]
  (let [f (compile-route :head path args body)]
    `(with-meta ~f ~(meta f))))

(defmacro OPTIONS "Generate an OPTIONS route."
  [path args & body]
  (let [f (compile-route :options path args body)]
    `(with-meta ~f ~(meta f))))

(defmacro PATCH "Generate a PATCH route."
  [path args & body]
  (let [f (compile-route :patch path args body)]
    `(with-meta ~f ~(meta f))))

(defmacro ANY "Generate a route that matches any method."
  [path args & body]
  (let [f (compile-route :any path args body)]
    `(with-meta ~f ~(meta f))))

(defn- remove-suffix [path suffix]
  (subs path 0 (- (count path) (count suffix))))

(defn- wrap-context [handler]
  (fn [request]
    (let [uri     (:uri request)
          path    (:path-info request uri)
          context (or (:context request) "")
          subpath (-> request :route-params :__path-info)]
      (handler
       (-> request
           (assoc :path-info (if (= subpath "") "/" subpath))
           (assoc :context (remove-suffix uri subpath))
           (update-in [:params] dissoc :__path-info)
           (update-in [:route-params] dissoc :__path-info))))))

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
