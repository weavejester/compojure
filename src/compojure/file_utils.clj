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

(defn copy-stream
  "Copy the contents of an InputStream into an OutputStream."
  ([in out]
    (copy-stream in out 4096))
  ([#^InputStream in, #^OutputStream out, bufsize]
    (let [buffer (make-array Byte/TYPE bufsize)]
      (loop [len (.read in buffer)]
        (when (pos? len)
          (.write out buffer 0 len)
          (recur (.read in buffer)))))))

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
