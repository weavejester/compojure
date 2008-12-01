(ns fact)

(defn rand-ints
  "Generate an infinite sequence of random integers."
  ([]
    (rand-ints -65535 65535))
  ([min max]
    (repeatedly #(+ (rand-int (- max min)) min))))

(defn rand-elems
  "Generate an infinite sequence of random elements from a collection."
  [coll]
  (let [v (vec coll)]
    (repeatedly
      #(v (rand-int (count v))))))

(defn rand-seqs
  "Generate an infinite sequence of random length sequences created by
  a function f."
  ([f]
    (rand-seqs f 0 100))
  ([f min max]
    (map #(take % (f))
          (rand-ints min max))))

(def ascii-lower   "abcdefghijklmnopqrstuvwxyz")
(def ascii-upper   "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
(def ascii-letters (str ascii-upper ascii-lower))
(def digits        "01234567890")
(def punctuation   ".,:;!?\"'`<>()[]{}-")
(def white-space   " \t\n")

(defn rand-strs
  "Generate a sequence of random strings."
  ([]
    (rand-strs
      (str ascii-letters digits punctuation white-space)))
  ([chars]
    (rand-strs chars 0 100))
  ([chars min max]
    (map (partial apply str)
         (rand-seqs #(rand-elems chars) min max))))

(defstruct fact-info
  :doc
  :test
  :pending?
  :params
  :values)

(defmacro fact
  "Define a documented fact that can be verified via a series of test values
  applied to a test expression. If the expression evaluates to true for all
  values, the fact is verified.

  e.g. (fact \"The length of a list equals the sum of the length of its parts\"
         [x (rand-seqs rand-ints)
          y (rand-seqs rand-ints)]
         (= (count (concat x y))
            (+ (count x) (count y))))"
  ([doc]
   `(fact ~doc nil))
  ([doc value-map & expr]
    (let [pairs  (partition 2 value-map)
          params (map first pairs)
          values (map second pairs)]
     `(def ~(gensym "fact")
        (struct-map fact-info
          :doc      ~doc
          :test     (fn [~@params] ~@expr)
          :pending? ~(nil? value-map)
          :params  '~(vec params)
          :values   ~(vec values))))))

(defmacro stub
  "Create function stubs for isolated unit tests.
  e.g. (stub [(f 1 2) 3
              (f 3 2) 5]
         (= (+ (f 1 2) (f 3 2))
            8))"
  [stubs & body]
  (let [stub-pairs (partition 2 stubs)
        fold-map   (fn [acc [[f & args] ret]]
                      (assoc-in acc [f `(list ~@args)] ret))
        bind-stub  (fn [[fname fhash]]
                     `(~fname (fn [& args#] (~fhash args#))))]
    `(binding
       [~@(mapcat bind-stub (reduce fold-map {} stub-pairs))]
       ~@body)))

(def #^{:doc "The maximum amount of test values to use per fact."}
  *max-amount* 50)

(defn- test-cases
  "Make a sequence of test cases from a number of test value sequences. The
  number of test cases is limited to *max-amount*. If the values sequences
  are of uneven length, the sequences are repeated up to the length of the
  largest value sequence."
  [vals]
  (if (seq vals)
    (let [bounded-vals   (map #(take *max-amount* %) vals)
          max-count      (apply max (map count bounded-vals))
          same-size-vals (map #(take max-count (cycle %)) bounded-vals)]
      (apply map vector same-size-vals))
    [[]]))

(defn- run-tests
  "Run a function with a collection of test-cases and return the results."
  [func test-cases]
  (map
    (fn [vals]
      (try
        (if (apply func vals)
          [:success vals]
          [:failure vals])
      (catch Exception e
        [:exception [e vals]])
      (catch Error e
        [:exception [e vals]])))
    test-cases))

(defn- filter-category
  "Filter a sequence of results matching the supplied category."
  [category results]
  (map second
    (filter #(= category (first %)) results)))

(defstruct result
  :fact
  :successes
  :failures
  :exceptions)

(defn verify
  "Verify a single fact."
  [fact]
  (if (fact :pending?)
    (struct result fact nil nil nil)
    (let [results (run-tests
                    (fact :test)
                    (test-cases (fact :values)))]
      (struct-map result
        :fact       fact
        :successes  (filter-category :success   results)
        :failures   (filter-category :failure   results)
        :exceptions (filter-category :exception results)))))

(defn- get-facts
  "Get all the functions beginning with 'fact' from a namespace."
  [ns]
  (map (comp var-get second)
       (filter
         (fn [[k f]] (.startsWith (name k) "fact"))
         (ns-publics ns))))

(defn verify-facts
  "Get a lazy list of results from all the facts in a namespace."
  ([]   (verify-facts *ns*))
  ([ns] (map verify (get-facts ns))))

(def #^{:doc "PrintWriter to which test results are printed; defaults to
  System.out."}
  *test-out* System/out)

(defn- format-params
  "Format a collection of parameters and values to a string."
  [params values]
  (pr-str
    (apply hash-map
      (interleave params values))))

(defn- format-exception
  "Format an exception thrown by a test as a string."
  [result]
  (let [[exception values] (first (result :exceptions))]
    (str "\n  EXCEPTION: "
      (format-params ((result :fact) :params) values)
      "\n  => " exception)))

(defn- format-failure
  "Format a failed result as a string."
  [result]
  (str "\n  FAILURE: "
    (format-params ((result :fact) :params)
                   (first (result :failures)))))

(defn- format-result
  "Format a single result from a verified fact."
  [result]
  (let [fact (result :fact)]
    (str
      "- " (fact :doc)
      (if (fact :pending?)
        " (pending)"
        (cond
          (seq (result :exceptions)) (format-exception result)
          (seq (result :failures))   (format-failure result))))))

(defn print-results
  "Prints a summary of the results from a set of verified facts to *test-out*."
  [& results]
  (let [results (apply concat results)]
    (doseq [result results]
      (.println *test-out* (format-result result)))
    (.println *test-out*
      (str (count results) " facts, "
           (count (filter (comp :pending? :fact) results)) " pending, "
           (count (filter (comp seq :failures) results)) " failed, "
           (count (filter (comp seq :exceptions) results)) " exceptions"))))
