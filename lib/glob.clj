;; glob.clj -- globbing support for Clojure

(clojure/in-ns 'glob)
(clojure/refer 'clojure)
(lib/use compojure file-utils)

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
