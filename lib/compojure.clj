(in-ns 'compojure)
(clojure/refer 'clojure)

(import '(javax.servlet.http HttpServlet))

;;;;; General-use functions ;;;;;

(defn includes?
  "Returns true if x is contained in coll, else false."
  [x coll]
  (some (partial = x) coll))

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

(defn name-str
  "Finds the name for named objects, otherwise uses str."
  [x]
  (if (instance? clojure.lang.Named x)
    (name x)
    (str x)))

(defn join-str
  "Join a sequence of strings together with an optional separator string."
  ([coll]
    (apply str coll))
  ([coll sep]
    (reduce
      (fn [a b] (str a sep b))
      (str (first coll))
      (rest coll))))

(defn chunks
  "Group n consecutive items in a sequence together.
  e.g. (chucks 3 [1 2 3 4 5]) -> ((1 2 3) (4 5))"
  [n coll]
  (if coll
    (lazy-cons (take n coll)
               (chunks n (drop n coll)))))

(def otherwise true)   ; Useful for cond

(defmacro in-ns*
  "Changes namespace and refers the clojure and compojure namespaces."
  [& body]
  `(do (in-ns ~@body)
       (refer '~'clojure)
       (refer '~'compojure)))

(def #^{:private true} *loaded-files* '())

(defn require
  "Load the file if and only if it has not been loaded previously."
  [file]
  (when-not (includes? file *loaded-files*)
    (def *loaded-files*
      (cons file *loaded-files*))
    (load-file file)
    true))

(defmacro return
  "A do block that will always return the argument x."
  [x & body]
  `(let [x# ~x]
     (do ~@body x#)))

;;;;; File and stream functions ;;;;;

(defn file
  "Returns an instance of java.io.File."
  ([name]          (new java.io.File name))
  ([parent name]   (new java.io.File parent name))
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

(defn split-path
  "Splits a path up into its parts."
  [path]
  (loop [parts nil, path (file path)]
    (let [parts (cons (. path (getName)) parts)]
      (if-let parent (. path (getParent))
        (recur parts (file parent))
        parts))))

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
  (re-find (re-pattern "\\*\\*") glob))

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
    (glob pattern "."))
  ([pattern path]
    (glob-parts (split-path pattern) (file path))))

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
    (require (str (file "./modules" (str m) "init.clj")))))
