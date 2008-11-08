(ns test.compojure.html
  (:use fact
        (compojure html)))

(defn rand-tags
  "Generate a sequence of random tag names." []
  (rand-strs (str ascii-letters "-") 1 5))

(fact "Tag vectors can be empty"
  [tag (rand-tags)]
  (= (xml [tag])
     (str "<" tag " />")))

(fact "Tag vectors can contain strings"
  [tag     (rand-tags)
   content (rand-strs)]
  (= (xml [tag content])
     (str "<" tag ">" content "</" tag ">")))

(fact "Tag vectors concatenate their contents"
  [tag      (rand-tags)
   contents (rand-seqs rand-strs 1 10)]
  (= (xml (apply vector tag contents))
     (str "<" tag ">" (apply str contents) "</" tag ">")))

(fact "Sequences in tag vectors are expanded out"
  [tag      (rand-tags)
   contents (rand-seqs rand-strs 1 10)]
  (= (xml (apply vector tag contents))
     (xml [tag contents])))

(fact "Tag vectors can be nested"
  [outer-tag  (rand-tags)
   before     (rand-strs)
   inner-tag  (rand-tags)
   inner-str  (rand-strs)
   after      (rand-strs)]
  (= (xml [outer-tag
            before [inner-tag inner-str] after])
     (xml [outer-tag
            before (xml [inner-tag inner-str]) after])))

(fact "Tag vectors can have attribute maps")

(fact "HTML tag vectors have syntax sugar for class attributes")

(fact "HTML tag vectors have syntax sugar for id attributes")
