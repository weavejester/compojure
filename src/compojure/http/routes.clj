;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.http.routes:
;; 
;; Functions compiling and matching routes contructed using the Rails routing
;; syntax.

(ns compojure.http.routes
  (:use compojure.str-utils))

;; A structure that represents a route
(defstruct url-route
  :regex
  :keywords)

;; Functions for lexing a string
(defn- apply-action
  "Apply an action to the matcher if the action is not a function."
  [action matcher]
  (if (and (ifn? action)
	   (not (keyword? action)))
    (action matcher)
    action))

(defn- lex-1
  "Lex one symbol from a string, and return the symbol and trailing source."
  [src clauses]
  (some
    (fn [[re action]]
      (let [matcher (re-matcher re src)]
        (if (.lookingAt matcher)
          [(apply-action action matcher)
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

;; Functions for compiling and matching routes
(defn compile-route
  "Compile a string using the routes syntax into a url-route struct."
  [route]
  (let [splat #"\*"
        word  #":(\w+)"
        path  #"[^:*]+"]
    (struct url-route
      (re-pattern
        (apply str
          (lex route
            splat "(.*?)"
            word  "([^/.,;?]+)"
            path  #(re-escape (.group %)))))
      (vec
        (filter (complement nil?)
          (lex route
            splat :*
            word  #(keyword (.group % 1))
            path  nil))))))

(defn- assoc-keywords-with-groups
  "Create a hash-map from a series of regex match groups and a collection of
  keywords."
  [groups keywords]
  (reduce
    (partial merge-with
      #(if (vector? %1)
         (conj %1 %2)
         (vector %1 %2)))
    {}
    (map hash-map keywords (rest groups))))

(defn match-route
  "Match a path against a parsed route. Returns a map of keywords and their
  matching path values."
  [route path]
  (let [matcher (re-matcher (route :regex) path)]
    (if (.matches matcher)
      (assoc-keywords-with-groups
        (re-groups matcher)
        (route :keywords)))))
