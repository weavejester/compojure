(ns compojure.core
  "A concise syntax for generating Ring handlers."
  (:use clojure.contrib.def
        clout.core
        compojure.response
        [ring.middleware params
                         keyword-params
                         nested-params
                         cookies]))

(defn- method-matches
  "True if this request matches the supplied method."
  [method request]
  (let [request-method (request :request-method)
        form-method    (get-in request [:form-params "_method"])]
    (or (nil? method)
        (if (and form-method (= request-method :post))
          (= (.toUpperCase (name method)) form-method)
          (= method request-method)))))

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

(defn- assoc-route-params
  "Associate route parameters with the request map."
  [request params]
  (merge-with merge request {:route-params params, :params params}))

(defn- assoc-&-binding [binds req sym]
  (assoc binds sym `(dissoc (:params ~req) ~@(map keyword (keys binds)))))

(defn- assoc-symbol-binding [binds req sym]
  (assoc binds sym `(get-in ~req [:params ~(keyword sym)])))

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

(defmacro bind-request
  "Bind a request to a collection of symbols. The collection can be a Clojure
  map destructuring binding for the request map, or it can be a vector of
  parameter bindings."
  [request bindings & body]
  (if (vector? bindings)
    `(let [~@(vector-bindings bindings request)] ~@body)
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
               (render (do ~@body) request#))))))))

(defn routing
  "Apply a list of routes to a Ring request map."
  [request & handlers]
  (some #(% request) handlers))

(defn routes
  "Create a Ring handler by combining several handlers into one."
  [& handlers]
  (-> #(apply routing % handlers)
      wrap-keyword-params
      wrap-nested-params
      wrap-params
      wrap-cookies))

(defmacro defroutes
  "Define a Ring handler function from a sequence of routes. The name may be
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

(defmacro ANY "Generate a route that matches any method."
  [path args & body]
  (compile-route nil path args body))

(defn- keyword->middleware
  "Turn a keyword into a wrapper function symbol.
  e.g. :test => wrap-test
       (:test x) => (wrap-test x)"
  [kw]
  (letfn [(mw-sym [x]
            (symbol (namespace x) (str "wrap-" (name x))))]
    (cond
      (keyword? kw)
        (mw-sym kw)
      (and (seq? kw) (keyword? (first kw)))
        (cons (mw-sym (first kw)) (rest kw))
      :else
        kw)))

(defmacro wrap!
  "DEPRECATED: Use '->' instead.
  Wrap a handler in middleware functions. Uses the same syntax as the ->
  macro. Additionally, keywords may be used to denote a leading 'wrap-'.
  e.g.
    (wrap! foo (:session cookie-store))
    => (wrap! foo (wrap-session cookie-store))
    => (def foo (wrap-session foo cookie-store))"
  {:deprecated "0.6.0"}
  [handler & funcs]
  (let [funcs (map keyword->middleware funcs)]
    `(alter-var-root
       (var ~handler)
       (constantly (-> ~handler ~@funcs)))))
