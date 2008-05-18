(in-ns 'compojure)
(clojure/refer 'clojure)

(defn includes?
  "Returns true if x is contained in coll, else false."
  [x coll]
  (some (partial = x) coll))

(defn escape
  "Escape a set of special characters chars in a string s."
  [chars s]
  (apply str
    (mapcat #(if (includes? % chars) [\\ %] [%]) s)))

(defn grep
  "Filters a seq by a regular expression."
  [re coll]
  (filter #(re-matches re %) coll))

(defn re-escape
  "Escape all special regex chars in a string s."
  [s]
  (escape "\\.*+|?()[]{}$^" s))

(defn re-find-all
  "Repeat re-find for matcher m until nil, and return the seq of results."
  [m]
  (doall (take-while identity
    (map re-find (repeat m)))))

(defn file
  "Returns an instance of java.io.File."
  ([name]        (new java.io.File name))
  ([parent name] (new java.io.File parent name)))

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
  (loop [parts (list) path (file path)]
    (let [parts (cons (. path (getName)) parts)]
      (if-let parent (. path (getParent))
        (recur parts (file parent))
        parts))))

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
  (doseq f (glob g) (load-file (str f))))
