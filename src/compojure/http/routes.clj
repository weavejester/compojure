;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.http.routes
  "Macros and functions for compiling routes in the form (method path & body)
   into stand-alone functions that return the return value of the body, or the
   keyword :next if they don't match."
  (:use compojure.http.request
        compojure.http.response
        compojure.http.session
        compojure.str-utils
        compojure.map-utils
        compojure.control
        compojure.encodings)
  (:import java.util.regex.Pattern
           java.util.Map))

;; Functions for lexing a string

(defn- lex-1
  "Lex one symbol from a string, and return the symbol and trailing source."
  [src clauses]
  (some
    (fn [[re action]]
      (let [matcher (re-matcher re src)]
        (if (.lookingAt matcher)
          [(if (fn? action) (action matcher) action)
           (.substring src (.end matcher))])))
    (partition 2 clauses)))

(defn- lex
  "Lex a string into tokens by matching against regexs and evaluating
   the matching associated function."
  [src & clauses]
  (loop [results []
         src     src
         clauses clauses]
    (if-let [[result src] (lex-1 src clauses)]
      (let [results (conj results result)]
        (if (= src "")
          results
          (recur results src clauses))))))

;; Functions for matching URIs using a syntax borrowed from Ruby frameworks
;; like Sinatra and Rails.

(defstruct uri-matcher
  :regex
  :keywords)

(defn compile-uri-matcher
  "Compile a path string using the routes syntax into a uri-matcher struct."
  [path]
  (let [splat   #"\*"
        word    #":([A-Za-z][\w-]*)"
        literal #"(:[^A-Za-z*]|[^:*])+"]
    (struct uri-matcher
      (re-pattern
        (apply str
          (lex path
            splat   "(.*?)"
            word    "([^/.,;?]+)"
            literal #(re-escape (.group %)))))
      (vec
        (remove nil?
          (lex path
            splat   :*
            word    #(keyword (.group % 1))
            literal nil))))))

;; Don't compile paths more than once.
(decorate-with memoize compile-uri-matcher)

(defmulti compile-matcher
  "Compile a string or regex into a form suitable for buing passed to the
  match-uri function."
  class)

(defmethod compile-matcher String
  [path]
  (compile-uri-matcher path))

(defmethod compile-matcher Pattern
  [re]
  re)

(defn- assoc-keywords-with-groups
  "Create a hash-map from a series of regex match groups and a collection of
  keywords."
  [groups keywords]
  (reduce
    (fn [m [k v]] (assoc-vec m k v))
    {}
    (map vector keywords groups)))

(defmulti match-uri
  "Match a URL against a compiled URI-matcher or a regular expression. Returns
  the matched URI keywords as a map, or the matched regex groups as a vector."
  (fn [matcher uri] (class matcher)))

(defmethod match-uri Map
  [uri-matcher uri]
  (let [matcher (re-matcher (uri-matcher :regex) (or uri "/"))]
    (if (.matches matcher)
      (assoc-keywords-with-groups
        (map urldecode (re-groups* matcher))
        (uri-matcher :keywords)))))

(defmethod match-uri Pattern
  [uri-pattern uri]
  (let [matches (re-matches uri-pattern (or uri "/"))]
    (if matches
      (if (vector? matches)
        (vec (map urldecode (rest matches)))
        []))))

(defn match-method
  "True if this request matches the supplied method."
  [method request]
  (let [request-method (request :request-method)
        form-method    (-> request :form-params :_method)]
    (or (nil? method)
        (if (and form-method (= request-method :post))
          (= (upcase-name method) form-method)
          (= method request-method)))))

(defn request-url
  "Return the complete URL for the request."
  [request]
  (str
    (name (:scheme request))
    "://"
    (get-in request [:headers "host"])
    (:uri request)))

(defn absolute-url?
  "True if the string is an absolute URL."
  [s]
  (re-find #"^[a-z+.-]+://" s))

(defn get-matcher-uri
  "Get the appropriate request URI for the given path pattern."
  [path request]
  (if (and (string? path) (absolute-url? path))
    (request-url request)
    (:uri request)))

(defmacro request-matcher
  "Compiles a function to match a HTTP request against the supplied method
  and path template. Returns a map of the route parameters if the is a match,
  nil otherwise. Precompiles the route when supplied with a literal string."
  [method path]
  (let [matcher (if (or (string? path) (instance? Pattern path))
                  (compile-matcher path)
                 `(compile-matcher ~path))]
   `(fn [request#]
      (and
        (match-method ~method request#)
        (match-uri ~matcher (get-matcher-uri ~path request#))))))

;; Functions and macros for generating routing functions. A routing function
;; returns :next if it doesn't match, and any other value if it does.

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
  `(let [matcher# (request-matcher ~method ~path)]
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
