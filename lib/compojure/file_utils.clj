;; clojure.file-utils
;;
;; Various utility functions for handling and retrieving files.

(ns compojure.file-utils
  (:import (java.io File
                    FileReader
                    PushbackReader
                    InputStream
                    OutputStream)
           (clojure.lang RT)))
(defn file
  "Returns an instance of java.io.File."
  ([name]          (new File name))
  ([parent name]   (new File parent name))
  ([p q & parents] (reduce file (file p q) parents)))

(defn resource
  "Returns a URL to a file in a resource."
  [name]
  (.getResource (.baseLoader RT) name))

(defn list-dir
  "List all the files in a directory."
  [dir]
  (seq (. (file dir) (list))))

(defn pipe-stream
  "Pipe the contents of an InputStream into an OutputStream."
  ([in out]
    (pipe-stream in out 4096))
  ([#^InputStream in #^OutputStream out bufsize]
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
  (map
    (memfn getName)
    (reverse
      (file-parents (file path)))))

(defn read-file
  "Repeatedly read from a file and return the sequence of results."
  [filename]
  (let [eof    (new Object)
        reader (new FileReader (file filename))
        stream (new PushbackReader reader)]
    (take-while
      #(not (identical? % eof))
       (repeatedly #(read stream false eof)))))
