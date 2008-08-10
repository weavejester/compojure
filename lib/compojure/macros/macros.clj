;; compojure.macros -- convinient macros for Compojure

(clojure/in-ns 'compojure.macros)
(clojure/refer 'clojure)

(defmacro return
  "A do block that will always return the argument 'x'."
  [x & body]
  `(let [x# ~x]
     (do ~@body x#)))

(defmacro domap
  "Similar to doseq, but collects the results into a sequence, like map."
  [item list & body]
  `(map (fn [~item] ~@body) ~list))
