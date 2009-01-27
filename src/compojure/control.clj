;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.control:
;; 
;; Various macros for controling program flow.

(ns compojure.control
  (:use clojure.contrib.seq-utils))

(defmacro return
  "A do block that will always return the argument 'x'."
  [x & body]
  `(let [x# ~x]
     (do ~@body x#)))

(defmacro domap
  "Similar to doseq, but collects the results into a sequence."
  [[item list] & body]
  `(map (fn [~item] ~@body) (doall ~list)))

(defmacro redef
  "Redefine an existing value, keeping the metadata intact."
  [name value]
  `(let [m# (meta #'~name)
         v# (def ~name ~value)]
     (.setMeta v# (merge m# (meta #'~name)))
     v#))

(defmacro decorate-with
  "Wrap functions in a decorator."
  [decorator & funcs]
  `(do ~@(domap [f funcs]
          `(redef ~f (~decorator ~f)))))

(defmacro decorate-bind
  "Wrap named functions in a decorator for a bounded scope."
  [decorator funcs & body]
  `(binding
     [~@(mapcat (fn [f] [f (list decorator f)]) funcs)]
     ~@body))

(defmacro deftmpl
  "Define a template function. Arguments are passed via key-value pairs.
  e.g. (deftmpl foo [bar baz] (+ bar baz))
       (foo :bar 1 :baz 2)"
  [name doc? & body]
  (let [[doc? params & body]
          (if (string? doc?)
            (list* [doc?] body)
            (list* nil doc? body))]
   `(defn ~name
      ~@doc?
       [& param-map#]
       (let [{:keys ~params} (apply hash-map param-map#)]
        ~@body))))
