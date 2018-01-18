(ns compojure.coercions
  "Functions for coercing route parameters into different types.")

(defn as-int
  "Parse a string into an integer, or `nil` if the string cannot be parsed."
  [s]
  (try
    (Long/parseLong s)
    (catch NumberFormatException _ nil)))

(def ^:private uuid-pattern
  #"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")

(defn as-uuid
  "Parse a string into a UUID, or `nil` if the string cannot be parsed."
  [s]
  (when (re-matches uuid-pattern s)
    (try
      (java.util.UUID/fromString s)
      (catch IllegalArgumentException _ nil))))
