;; Simple parser library
(init-ns 'compojure.parser)

(defn- apply-action
  [action matcher]
  (if (and (instance? clojure.lang.IFn action)
           (not (keyword? action)))
    (action matcher)
    action))

(defn- parse-1
  [src & clauses]
  (some
    (fn [[re action]]
      (let [matcher (re-matcher re src)]
        (if (.lookingAt matcher)
          [(.substring src (.end matcher))
           (apply-action action matcher)])))
    (partition 2 clauses)))

(defn parse
  "Parse a string into tokens by matching against regexs and evaluating
   the matching associated function."
  [src & clauses]
  (loop [src     src
         results []
         clauses clauses]
    (if-let parsed (apply parse-1 src clauses)
      (let [[src result] parsed
            results      (conj results result)]
        (if (= src "")
          results
          (recur src results clauses))))))
