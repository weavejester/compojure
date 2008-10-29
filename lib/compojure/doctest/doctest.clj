(ns compojure.doctest
  (:use    (clojure.contrib def))
  (:import (java.io BufferedReader
                    PushbackReader
                    StringReader
                    Reader)
           (java.util.regex Pattern)))

(defvar *test-out* (. System err)
  "PrintWriter to which test results are printed; defaults to
   System.err.")

(defstruct result
  :expr
  :success
  :actual
  :expected)

(defn slurp-reader
  "Returns a string of the contents of the reader."
  [#^Reader reader]
  (with-open br (new BufferedReader reader)
    (let [sb (new StringBuilder)]
      (loop [c (.read br)]
        (if (neg? c)
          (str sb)
          (do (.append sb (char c))
              (recur (.read br))))))))

(defn read-str
  "Parse a string into a vector containing a Clojure expression and the text
  left over after the end of the expression."
  [expr]
  (let [reader (new PushbackReader (new StringReader expr))]
    [(read reader)
     (slurp-reader reader)]))

(defn- test-pair
  "Parse and evaluate an expression, and compare the output to an expected
   value."
  [[expr expected]]
  (let [expected (pr-str (first (read-str (.trim expected))))
        capture #(.trim (with-out-str (prn (eval %))))]
    (try
      (let [value (capture expr)]
        (struct result 
          expr (= value expected)
          value expected))
      (catch Exception value
        (struct result
          expr (.startsWith (str value) expected)
          value expected)))))

(defn literate-test
  "Run a series of tests extracted from a literate file."
  [tests]
  (map test-pair tests))

(defn print-results
  "Prints a summary of the results from a literate test to *test-out*."
  [results]
  (doseq result (filter (complement :success) results)
    (.println *test-out*
      (str (pr-str (result :expr))
           "\n  expected: " (result :expected)
           "\n  actual:   " (result :actual) "\n")))
  (.println *test-out*
    (str "Success: " (count (filter :success results)) "/" (count results))))

(defn tests-from-text
  "Pull tests from a text file."
  [text]
  (if text
    (let [dot-all     (.DOTALL Pattern)
          re-expr     (.compile Pattern "=>(.*)$" dot-all)
          re-expected (.compile Pattern "(.*?)(\n\n.*)?$" dot-all)]
      (if-let next-test (re-find re-expr text)
        (let [[expr text]       (read-str (next-test 1))
              [_ expected text] (re-matches re-expected text)]
          (lazy-cons
            [expr expected]
            (tests-from-text text)))))))
