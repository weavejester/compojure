;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.str-utils
  "Utility functions for manipulating strings."
  (:use clojure.contrib.seq-utils)
  (:use clojure.contrib.str-utils)
  (:import clojure.lang.Named))

(defn escape
  "Returns a string with each occurance of a character in
  chars escaped."
  [chars #^String string]
  (let [charset (set chars)]
    (apply str
      (mapcat
        #(if (contains? charset %) [\\ %] [%])
        string))))

(defn map-str
  "Map a function to a collection, then concatenate the results into a
  string."
  [func coll]
  (apply str (map func coll)))

(defn indent
  "Indent each line in a string of text. Defaults to an indentation of two
  spaces."
  ([text]
    (indent text "  "))
  ([text spacer]
    (map-str
      #(str spacer % "\n")
       (re-split #"\n" text))))

(defn str*
  "A version of str that prefers the names of Named objects.
  e.g (str \"Hello \" :World)  => \"Hello :World\"
      (str* \"Hello \" :World) => \"Hello World\""
  [& args] 
  (map-str
    #(if (instance? Named %) (name %) (str %))
    args))

(defn re-escape
  "Escape all special regex chars in string."
  [string]
  (escape "\\.*+|?()[]{}$^" string))

(defn re-groups*
  "More consistant re-groups that always returns a vector of groups, even if
  there is only one group."
  [matcher]
  (for [i (range (.groupCount matcher))]
    (.group matcher (inc i))))

(defn blank?
  "True if s = \"\" or nil"
  [s]
  (or (nil? s) (= s "")))

(defn lines
  "Concatenate a sequence of strings into lines of a single string."
  [coll]
  (str-join "\n" coll))

(defn capitalize
  "Uppercase the first letter of a string, and lowercase the rest."
  [s]
  (str (.toUpperCase (subs s 0 1))
       (.toLowerCase (subs s 1))))

(defn grep
  "Filter a collection of strings by a regex."
  [re coll]
  (filter (partial re-find re) coll))

(defn upcase-name
  "Upcase a symbol or keyword's name."
  [sym]
  (. (name sym) toUpperCase))
