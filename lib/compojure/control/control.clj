;; Macros and functions for controling program flow
(ns compojure.control)

(defn ifn
  "Equivalent to: (if (pred x) x (func x))"
  [pred func x]
  (if (pred x) x (func x)))

(defmacro return
  "A do block that will always return the argument 'x'."
  [x & body]
  `(let [x# ~x]
     (do ~@body x#)))

(defmacro domap
  "Similar to doseq, but collects the results into a sequence."
  [item list & body]
  `(map (fn [~item] ~@body) ~list))

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
  `(do ~@(domap f funcs
          `(redef ~f (~decorator ~f)))))
