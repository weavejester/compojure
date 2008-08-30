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
