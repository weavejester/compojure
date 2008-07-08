(in-ns 'compojure)
(clojure/refer 'clojure)

(import '(clojure.lang Named)
        '(java.lang IllegalArgumentException)
        '(javax.servlet.http HttpServlet)
        '(java.io File FileReader PushbackReader))

;;;;; General-use functions ;;;;;

(defn includes?
  "Returns true if x is contained in coll, else false."
  [x coll]
  (some (partial = x) coll))

(defn parse-int
  "Parse a integer contained in a string s."
  [s]
  (. Integer (parseInt s)))

(def otherwise true)   ; Useful for cond

(defmacro return
  "A do block that will always return the argument x."
  [x & body]
  `(let [x# ~x]
     (do ~@body x#)))

(defmacro defconj
  "Short for (def name (conj name value))"
  [name value]
  `(def ~name (conj ~name ~value)))

(defn rmap
  "Reversed map."
  [func coll]
  (reverse (map func coll)))

(defn default
  "Change a function so that it returns a default value instead of nil"
  [func default]
  (fn [& args]
    (let [value (apply func args)]
      (if (nil? value) default value))))

(defn kwargs
  "Adds any value preceeded by a keyword to a hash map, and returns the map
  and a sequence of remaining values.
  e.g. (kwargs [:baz 5 \"foo\" 10 :bar 20])
       => [{:baz 5 :bar 20} (\"foo\" 10)]"
  [coll]
  (loop [options {}, args nil, coll (seq coll)]
    (if coll
      (let [key (first coll)]
        (if (keyword? key)
          (recur (assoc options key (second coll)) args (rrest coll))
          (recur options (cons key args) (rest coll))))
      [options (reverse args)])))

(defn tree-map
  "Map a function on each sub-sequence of a tree of sequences."
  [func tree]
  (func (map
          #(if (seq? %) (tree-map func %) %)
          tree)))

(defn try-seq
  "Try out (seq x), and return nil if it throws an exception."
  [x]
  (try (seq x)
       (catch IllegalArgumentException e
         nil)))

(defn flatten
  "Flatten a collection into a one-dimensional sequence."
  [coll]
  (if (seq coll)
    (let [[x & xs] coll]
      (if (try-seq x)
        (lazy-cat (flatten x) (flatten xs))
        (lazy-cons x (flatten xs))))
    coll))
  
;;;;; String functions ;;;;;

(defn str-map
  "Map a function to a collection, then concatenate the results into a string."
  [func coll]
  (apply str (map func coll)))

(defn str*
  "A version of str that prefers the names of Named objects.
  e.g (str \"Hello \" :World)  => \"Hello :World\"
      (str* \"Hello \" :World) => \"Hello World\""
  [& args] 
  (str-map 
    #(if (instance? Named %) (name %) (str %))
    args))

(defn str-join
  "Join a sequence of strings together with an optional separator string."
  ([coll]
    (apply str coll))
  ([coll sep]
    (reduce
      (fn [a b] (str a sep b))
      (str (first coll))
      (rest coll))))

(defn lines
  "Concatenate a sequence of strings into lines of a single string."
  [coll]
  (str-join coll "\n"))

(defn escape
  "Escape a set of special characters chars in a string s."
  [chars s]
  (apply str
    (mapcat #(if (includes? % chars) [\\ %] [%]) s)))

(defn re-escape
  "Escape all special regex chars in a string s."
  [s]
  (escape "\\.*+|?()[]{}$^" s))

(defn re-find-all
  "Repeat re-find for matcher m until nil, and return the seq of results."
  [m]
  (doall (take-while identity
    (map re-find (repeat m)))))

(defn re-split
  "Splits a string by a regex."
  [re s]
  (seq (. re (split s))))

(defn indent
  "Indent each line in a string of text. Defaults to an indentation of two
  spaces."
  ([text]
    (indent text "  "))
  ([text spacer]
    (str-map
      #(str spacer % "\n")
      (re-split #"\n" text))))

(defn wrout
  "Write a string to *out*."
  [s]
  (. *out* (write s)))

;;;;; File and stream functions ;;;;;

(defn file
  "Returns an instance of java.io.File."
  ([name]          (new File name))
  ([parent name]   (new File parent name))
  ([p q & parents] (reduce file (file p q) parents)))

(defn pipe-stream
  "Pipe the contents of an InputStream into an OutputStream."
  ([in out] (pipe-stream in out 4096))
  ([in out bufsize]
    (let [buffer (make-array (. Byte TYPE) bufsize)]
      (loop [len (. in (read buffer))]
        (when (pos? len)
          (. out (write buffer 0 len))
          (recur (. in (read buffer))))))))

(defn file-parents
  "Lazily iterate through all of the parents of a file."
  [file]
  (take-while identity
    (iterate (memfn getParentFile) file)))

(defn split-path
  "Splits a path up into its parts."
  [path]
  (rmap
    (memfn getName)
    (file-parents (file path))))

(def canonical
  (memfn getCanonicalFile))

(defn relative-path
  "Find the path relative to another, if possible."
  [base path]
  (apply file
    (rmap (memfn getName)
          (take-while
            (partial not= (canonical (file base)))
            (file-parents (canonical path))))))

(defn read-file
  "Repeatedly read from a file and return the sequence of results."
  [filename]
  (let [eof    (new Object)
        reader (new FileReader (file filename))
        stream (new PushbackReader reader)]
    (take-while
      #(not (identical? % eof))
       (repeatedly #(read stream false eof)))))

(def #^{:private true}
  *loaded-paths* #{})

(defn require
  "Load the file if and only if it has not been loaded previously."
  [filepath]
  (let [path (canonical (file filepath))]
    (when-not (contains? *loaded-paths* path)
      (defconj *loaded-paths* path)
      (load-file (str path))
      true)))

;;;;; Globbing functions ;;;;;

(defn- glob->regex
  "Turns a shallow file glob into a regular expression."
  [s]
  (re-pattern
    (.. (escape "\\.+|()[]{}$^" s)
        (replaceAll "\\*" ".*")
        (replaceAll "\\?" "."))))

(defn- recursive-glob?
  [glob]
  (re-find #"\\*\\*" glob))

(defn- glob-parts
  [parts path]
  (if parts
    (if (. path (isDirectory))
      (mapcat
       #(glob-parts (rest parts) %)
        (filter
         #(re-matches
            (glob->regex (first parts))
            (. % (getName)))
          (if (recursive-glob? (first parts))
            (file-seq path)
            (. path (listFiles))))))
    (list path)))

(defn glob
  "Find all files in a directory matching a glob."
  ([pattern]
    (glob-parts (split-path pattern) (file ".")))
  ([path pattern]
    (map
      #(relative-path path %)
       (glob-parts (split-path pattern) (file path)))))

(defn load-glob
  "Load all files matching a glob."
  [g]
  (doseq f (glob g)
    (load-file (str f))))

(defn require-glob
  "Require all files matching a glob."
  [g]
  (doseq f (glob g)
    (require (str f))))

;;;;; Framework functions ;;;;;

(defmacro in-ns*
  "Changes namespace and refers the clojure and compojure namespaces."
  [& body]
  `(do (in-ns ~@body)
       (refer '~'clojure)
       (refer '~'compojure)))

(defn new-servlet
  "Create a new servlet from a function that takes three arguments of types
  HttpServletContext, HttpServletRequest, HttpServletResponse."
  [func] 
  (proxy [HttpServlet] []
    (service [request response]
      (func (. this (getServletContext)) request response))))

(defn require-module
  "Require a set of modules. Allows you to write:
    (require-module 'http 'html)
  Instead of:
    (require \"modules/http/init.clj\")
    (require \"modules/html/init.clj\")"
  [& modules]
  (doseq m modules
    (require (str (file "modules" (str m) "init.clj")))))
