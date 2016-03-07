(ns compojure.core
  "A DSL for building Ring handlers from smaller routes.

  Compojure routes are semantically the same as Ring handlers, with the
  exception that routes may return `nil` to indicate they do not match.

  This namespace provides functions and macros for concisely constructing
  routes and combining them together to form more complex functions."
  (:require [compojure.response :as response]
            [clojure.tools.macro :as macro]
            [clout.core :as clout]
            [ring.util.codec :as codec]
            [medley.core :refer [map-vals]]))

(defn- method-matches? [method request]
  (let [request-method (request :request-method)
        form-method    (or (get-in request [:form-params "_method"])
                           (get-in request [:multipart-params "_method"]))]
    (if (and form-method (= request-method :post))
      (.equalsIgnoreCase (name method) form-method)
      (= method request-method))))

(defn- if-method [method handler]
  (fn [request]
    (cond
      (or (nil? method) (method-matches? method request))
        (handler request)
      (and (= :get method) (= :head (:request-method request)))
        (if-let [response (handler request)]
          (assoc response :body nil)))))

(defn- decode-route-params [params]
  (map-vals codec/url-decode params))

(defn- assoc-route-params [request params]
  (merge-with merge request {:route-params params, :params params}))

(defn- route-matches [route request]
  (let [path (:compojure/path request)]
    (clout/route-matches route (cond-> request path (assoc :path-info path)))))

(defn- if-route [route handler]
  (fn [request]
    (if-let [params (route-matches route request)]
      (handler (assoc-route-params request (decode-route-params params))))))

(defn- literal? [x]
  (if (coll? x)
    (every? literal? x)
    (not (or (symbol? x) (list? x)))))

(defn- prepare-route [route]
  (cond
    (string? route)
      (clout/route-compile route)
    (and (vector? route) (literal? route))
      (clout/route-compile
       (first route)
       (apply hash-map (rest route)))
    (vector? route)
      `(clout/route-compile
        ~(first route)
        ~(apply hash-map (rest route)))
    :else
      `(if (string? ~route)
         (clout/route-compile ~route)
         ~route)))

(defn- and-binding [req binds]
  `(dissoc (:params ~req) ~@(map keyword (keys binds)) ~@(map str (keys binds))))

(defn- symbol-binding [req sym]
  `(get-in ~req [:params ~(keyword sym)] (get-in ~req [:params ~(str sym)])))

(defn- application-binding [req sym func]
  `(~func ~(symbol-binding req sym)))

(defn- vector-bindings [args req]
  (loop [args args, binds {}]
    (if (seq args)
      (let [[x y z] args]
        (cond
          (= '& x)
          (recur (nnext args) (assoc binds y (and-binding req binds)))
          (= :as x)
          (recur (nnext args) (assoc binds y req))
          (and (symbol? x) (= :<< y) (nnext args))
          (recur (drop 3 args) (assoc binds x (application-binding req x z)))
          (symbol? x)
          (recur (next args) (assoc binds x (symbol-binding req x)))
          :else
          (throw (Exception. (str "Unexpected binding: " x)))))
      (mapcat identity binds))))

(defn- warn-on-*-bindings! [bindings]
  (when (and (vector? bindings) (contains? (set bindings) '*))
    (binding [*out* *err*]
      (println "WARNING: * should not be used as a route binding."))))

(defn- application-symbols [args]
  (loop [args args, syms '()]
    (if (seq args)
      (let [[x y] args]
        (if (and (symbol? x) (= :<< y))
          (recur (drop 3 args) (conj syms x))
          (recur (next args) syms)))
      (seq syms))))

(defmacro ^:no-doc let-request [[bindings request] & body]
  (warn-on-*-bindings! bindings)
  (if (vector? bindings)
    `(let [~@(vector-bindings bindings request)]
       ~(if-let [syms (application-symbols bindings)]
          `(if (and ~@syms) (do ~@body))
          `(do ~@body)))
    `(let [~bindings ~request] ~@body)))

(defn- wrap-route-middleware [handler]
  (fn [request]
    (if-let [mw (:route-middleware request)]
      ((mw handler) request)
      (handler request))))

(defn- wrap-route-info [handler route-info]
  (fn [request]
    (handler (assoc request :compojure/route route-info))))

(defn make-route
  "Returns a function that will only call the handler if the method and path
  match the request."
  [method path handler]
  (let [route-info [(or method :any) (str path)]]
    (if-method method
      (if-route path
        (-> (fn [request] (response/render (handler request) request))
            (wrap-route-middleware)
            (wrap-route-info route-info))))))

(defn compile-route
  "Compile a route in the form `(method path bindings & body)` into a function.
  Used to create custom route macros."
  [method path bindings body]
  `(make-route
    ~method ~(prepare-route path)
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
  (let [[name routes] (macro/name-with-attributes name routes)]
   `(def ~name (routes ~@routes))))

(defmacro GET "Generate a `GET` route."
  [path args & body]
  (compile-route :get path args body))

(defmacro POST "Generate a `POST` route."
  [path args & body]
  (compile-route :post path args body))

(defmacro PUT "Generate a `PUT` route."
  [path args & body]
  (compile-route :put path args body))

(defmacro DELETE "Generate a `DELETE` route."
  [path args & body]
  (compile-route :delete path args body))

(defmacro HEAD "Generate a `HEAD` route."
  [path args & body]
  (compile-route :head path args body))

(defmacro OPTIONS "Generate an `OPTIONS` route."
  [path args & body]
  (compile-route :options path args body))

(defmacro PATCH "Generate a `PATCH` route."
  [path args & body]
  (compile-route :patch path args body))

(defmacro ANY "Generate a route that matches any method."
  [path args & body]
  (compile-route nil path args body))

(defmacro rfn "Generate a route that matches any method and path."
  [args & body]
  `(#'wrap-route-middleware
    (fn [request#]
      (let [result#   (let-request [~args request#] ~@body)
            response# (response/render result# request#)]
        (if (and response# (= :head (:request-method request#)))
          (assoc response# :body nil)
          response#)))))

(defn- remove-suffix [path suffix]
  (subs path 0 (- (count path) (count suffix))))

(defn- if-context [route handler]
  (fn [request]
    (if-let [params (clout/route-matches route request)]
      (let [uri     (:uri request)
            path    (:path-info request uri)
            context (or (:context request) "")
            subpath (:__path-info params)
            params  (dissoc params :__path-info)]
        (handler
         (-> request
             (assoc-route-params (decode-route-params params))
             (assoc :path-info (if (= subpath "") "/" subpath)
                    :context   (remove-suffix uri subpath))))))))

(defn- context-route [route]
  (let [re-context {:__path-info #"|/.*"}]
    (cond
      (string? route)
        (clout/route-compile (str route ":__path-info") re-context)
      (and (vector? route) (literal? route))
        (clout/route-compile
         (str (first route) ":__path-info")
         (merge (apply hash-map (rest route)) re-context))
      (vector? route)
       `(clout/route-compile
         (str ~(first route) ":__path-info")
         ~(merge (apply hash-map (rest route)) re-context))
      :else
       `(clout/route-compile (str ~route ":__path-info") ~re-context))))

(defmacro context
  "Give all routes in the form a common path prefix and set of bindings.

  The following example demonstrates defining two routes with a common
  path prefix ('/user/:id') and a common binding ('id'):

      (context \"/user/:id\" [id]
        (GET \"/profile\" [] ...)
        (GET \"/settings\" [] ...))"
  [path args & routes]
  `(#'if-context
    ~(context-route path)
    (fn [request#]
      (let-request [~args request#]
        (routing request# ~@routes)))))

(defmacro let-routes
  "Takes a vector of bindings and a body of routes.

  Equivalent to:

      (let [...] (routes ...))"
  [bindings & body]
  `(let ~bindings (routes ~@body)))

(defn- pre-init [middleware]
  (let [proxy (middleware (fn [req] ((:route-handler req) req)))]
    (fn [handler]
      (fn [request]
        (proxy (assoc request :route-handler handler))))))

(defn wrap-routes
  "Apply a middleware function to routes after they have been matched."
  ([handler middleware]
     (let [middleware (pre-init middleware)]
       (fn [request]
         (let [mw (:route-middleware request identity)]
           (handler (assoc request :route-middleware (comp middleware mw)))))))
  ([handler middleware & args]
     (wrap-routes handler #(apply middleware % args))))
