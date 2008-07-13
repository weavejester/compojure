(clojure/in-ns 'compojure)
(clojure/refer 'clojure)

(lib/use str-utils
         seq-utils
         file-utils)

(import '(clojure.lang Named)
        '(javax.servlet.http HttpServlet))

(defn parse-int
  "Parse a integer contained in 'string'."
  [string]
  (. Integer (parseInt string)))

(defmacro return
  "A do block that will always return the argument 'x'."
  [x & body]
  `(let [x# ~x]
     (do ~@body x#)))

(defmacro domap
  "Similar to doseq, but collects the results into a sequence, like map."
  [item list & body]
  `(map (fn [~item] ~@body) ~list))

(defn str-map
  "Map a function to a collection, then concatenate the results into a
  string."
  [func coll]
  (apply str (map func coll)))

(defn escape
  "Returns a string with each occurance of a character in
  'chars' escaped."
  [chars #^String string]
  (apply str
    (mapcat
      #(if (includes? % chars) [\\ %] [%])
      string)))

(defn re-escape
  "Escape all special regex chars in 'string'."
  [string]
  (escape "\\.*+|?()[]{}$^" string))

(defn indent
  "Indent each line in a string of text. Defaults to an indentation of two
  spaces."
  ([text]
    (indent text "  "))
  ([text spacer]
    (str-map
      #(str spacer % "\n")
      (re-split #"\n" text))))

(defn re-find-all
  "Repeat re-find for matcher m until nil, and return the seq of
  results."
  [m]
  (doall (take-while identity
    (map re-find (repeat m)))))

(defn str*
  "A version of str that prefers the names of Named objects.
  e.g (str \"Hello \" :World)  => \"Hello :World\"
      (str* \"Hello \" :World) => \"Hello World\""
  [& args] 
  (str-map 
    #(if (instance? Named %) (name %) (str %))
    args))

(defn lines
  "Concatenate a sequence of strings into lines of a single string."
  [coll]
  (str-join "\n" coll))

(defn rmap
  "Maps a function onto a collection, then reverses the result."
  [func coll]
  (reverse (map func coll)))

(defn new-servlet
  "Create a new servlet from a function that takes three arguments of types
  HttpServletContext, HttpServletRequest, HttpServletResponse."
  [func] 
  (proxy [HttpServlet] []
    (service [request response]
      (func (. this (getServletContext)) request response))))

(defn relative-path
  "Find the path relative to another, if possible."
  [base path]
  (apply file
    (rmap (memfn getName)
          (take-while
            (partial not= (.canonicalFile (file base)))
            (file-parents (.canonicalFile path))))))

(defmacro module
  "Changes namespace and refers the clojure namespace."
  [name]
  `(do (in-ns '~name)
       (refer '~'clojure)
       (refer '~'lib)
       (refer '~'compojure)))

(defn wrout
  "Write a string to *out*."
  [string]
  (. *out* (write string)))
