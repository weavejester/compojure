;; Utility functions for manipulating strings
(init-ns 'compojure.str-utils)

(use '(clojure.contrib seq-utils
                       str-utils))

(import '(clojure.lang Named))

(defn escape
  "Returns a string with each occurance of a character in
  chars escaped."
  [chars #^String string]
  (apply str
    (mapcat
      #(if (includes? % chars) [\\ %] [%])
      string)))

(defn str-map
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
    (str-map
      #(str spacer % "\n")
      (re-split #"\n" text))))

(defn str*
  "A version of str that prefers the names of Named objects.
  e.g (str \"Hello \" :World)  => \"Hello :World\"
      (str* \"Hello \" :World) => \"Hello World\""
  [& args] 
  (str-map 
    #(if (instance? Named %) (name %) (str %))
    args))

(defn re-escape
  "Escape all special regex chars in string."
  [string]
  (escape "\\.*+|?()[]{}$^" string))

(defn blank?
  "True if s = \"\" or nil"
  [s]
  (or (nil? s) (= s "")))

(defn re-parts
  "Divide a string up at the boundaries matched by a regex."
  [re s]
  (let [m (re-matcher re s)
        f (fn [f i]
            (if-let group (re-find m)
              (let [parts  (lazy-cons [group true] (f f (.end m)))
                    before (.substring s i (.start m))]
                (if (blank? before)
                  parts
                  (lazy-cons [before false] parts)))
              (list [(.substring s i) false])))]
    (f f 0)))

(defn lines
  "Concatenate a sequence of strings into lines of a single string."
  [coll]
  (str-join "\n" coll))
