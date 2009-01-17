;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.http.handlers:
;;
;; 

(ns compojure.http.handlers
  (:use compojure.http.routes)
  (:import java.util.regex.Pattern))

(defn- route-clause
  "Generate a clause for matching the route method and path against a supplied
  method and path."
  [[s-method s-path] [r-method r-path _]]
  (let [route `(compile-route ~r-path)]
    (if (= r-method 'ANY)
     `(match-route ~route ~s-path)
     `(and (= ~s-method ~(name r-method))
           (match-route ~route ~s-path)))))

(defn- compile-handler
  "Compile a handler into an if statement."
  [method-and-path [method path & then] else]
  `(let [~'route ~(route-clause method-and-path [method path])
           resp#  (if ~'route (do ~@then) :next)]
     (if (not= resp# :next)
       resp#
      ~else)))
     
(defmacro handle-http
  "Given a method and path variable, compile a list of handlers into a set of
  nested if forms.
  e.g.
  (handle-http [method path]
    (GET \"/\" \"Hello World\")
    (ANY \"*\" [404 \"Not Found\"]))"
  [method-and-path & handlers]
  (if (seq handlers)
    (compile-handler method-and-path
      (first handlers)
     `(handle-http ~method-and-path ~@(rest handlers)))
    :next))
