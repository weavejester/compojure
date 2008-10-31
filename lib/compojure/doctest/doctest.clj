(ns compojure.doctest
  (:use (clojure.contrib def
                         fcase
                         seq-utils
                         str-utils))
  (:import (java.io BufferedReader
                    FileReader
                    PushbackReader
                    Reader
                    StringReader)
           (java.util.regex Pattern)))

(defvar *test-out* (. System err)
  "PrintWriter to which test results are printed; defaults to
   System.err.")

(defstruct result
  :expr
  :success?
  :expected
  :actual)

(defn- pushback-str
  [string]
  (new PushbackReader (new StringReader string)))

(defn- file-reader
  "Return a buffered file reader."
  [filename]
  (new BufferedReader (new FileReader filename)))

(defn- marked-lines
  "Filter lines beginning with a marker character."
  [lines]
  (filter (partial re-find #"^[>=`]( |$)") lines))

(defn- split-marker
  "Split a group of lines from its marker."
  [lines]
  [(ffirst lines)
   (str-join "\n"
     (map (partial re-sub #"^. ?" "") lines))])

(defn- expr-marker?
  "Is the line marked as an expression?"
  [[marker _]]
  (= marker \>))

(defn- result-matches?
  "Does a test result match the marker/value pair?"
  [marker expected actual]
  (case marker
    \= (= actual (read (pushback-str expected)))
    \` (= actual expected)))

(defn eval-test
  "Evaluate a test parsed from a file, returning true if the test passes."
  [[[[_ expr]] [[marker expected]]]]
  (let [expr     (read (pushback-str expr))
        actual   (eval expr)
        success? (result-matches? marker expected actual)]
    (struct result
       expr success? expected actual)))

(defn doctest
  "Evaluate the tests embedded in a file."
  [filename]
  (with-open reader (file-reader filename)
    (let [lines  (marked-lines (line-seq reader))
          groups (map split-marker
                      (partition-by first lines))
          tests  (partition-by expr-marker? groups)]
      (doall
        (map eval-test (partition 2 tests))))))

(defn print-results
  "Prints a summary of the results from a literate test to *test-out*."
  [results]
  (doseq result (filter (complement :success?) results)
    (.println *test-out*
      (str (pr-str (result :expr))
           "\n  expected: " (pr-str (result :expected))
           "\n  actual:   " (pr-str (result :actual)) "\n")))
  (.println *test-out*
    (str "Success: " (count (filter :success? results)) "/"
                     (count results))))
