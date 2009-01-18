(ns test.compojure.html
  (:use fact)
  (:use compojure.html))

(def names
  '(foo Bar foo-bar foo_bar fooBar foo:bar))

(fact "The first element of a tag vector defines the tag name"
  [tag names]
  (= (html [tag "Lorem Ipsum"])
     (str "<" tag ">Lorem Ipsum</" tag ">")))

(fact "Tag vectors can be empty"
  [tag names]
  (= (html [tag])
     (str "<" tag " />")))

(fact "Tags can be specified by keywords, symbols or strings"
  [tag [:xml 'xml "xml"]]
  (= (html [tag "Lorem Ipsum"])
     "<xml>Lorem Ipsum</xml>"))

(fact "Tag vectors can contain strings"
  [content (rand-strs)]
   (= (html [:xml content])
      (str "<xml>" content "</xml>")))

(fact "Tag vectors concatenate their contents"
  [contents (rand-seqs #(rand-strs) 1 100)]
  (= (html (apply vector :xml contents))
     (str "<xml>" (apply str contents) "</xml>")))

(fact "Sequences in tag vectors are expanded out"
  [contents (rand-seqs #(rand-strs))]
  (= (html (apply vector :xml contents))
     (html [:xml contents])))

(fact "Tag vectors can be nested"
  [[dom out]
     {[:a [:c]]          "<a><c /></a>"
      [:a "b" [:c] "d"]  "<a>b<c />d</a>"
      [:a [:b "c"]]      "<a><b>c</b></a>"
      [:a [:b [:c "d"]]] "<a><b><c>d</c></b></a>"}]
  (= (html dom) out))

(fact "Tag vectors can have attribute maps"
  [[attr-map attr-str]
     {{:a "1"}               "a=\"1\""
      {:b "1" :a "2"}        "a=\"2\" b=\"1\""
      {:b "2" :c "3" :a "1"} "a=\"1\" b=\"2\" c=\"3\""
      {:aBc "d"}             "aBc=\"d\""
      {:a_b "c"}             "a_b=\"c\""
      {:a:b "c"}             "a:b=\"c\""}]
  (= (html [:xml attr-map "Lorem Ipsum"])
     (str "<xml " attr-str ">Lorem Ipsum</xml>")))

(fact "Special characters are escaped in attribute values"
  [[original escaped]
     {"\"" "&quot;"
      "<"  "&lt;"
      ">"  "&gt;"
      "&"  "&amp;"}]
  (= (html [:img {:id original}])
     (str "<img id=\"" escaped "\" />")))

(fact "Strings, keywords and symbols can be keys in attribute maps"
  [attr [:id 'id "id"]]
  (= (html [:img {attr "a"}])
     "<img id=\"a\" />"))

(fact "An attribute map can have keys of different types"
  []
  (= (html [:xml {:a "1" 'b "2" "c" "3"}])
     "<xml a=\"1\" b=\"2\" c=\"3\" />"))

(def tags
  '(a span em strong code img p html body div script pre))

(fact "HTML tag vectors have CSS syntax sugar for class attributes"
  [tag   tags
   class names]
  (= (html [tag {:class class}])
     (html [(str tag "." class)])))

(fact "Multiple classes can be specified through the CSS syntax sugar"
  [tag     tags
   classes ["a" "a b" "a b c" "a b c d"]
   css     ["a" "a.b" "a.b.c" "a.b.c.d"]]
  (= (html [tag {:class classes}])
     (html [(str tag "." css)])))

(fact "HTML tag vectors have CSS syntax sugar for id attributes"
  [tag tags
   id  names]
  (= (html [tag {:id id}])
     (html [(str tag "#" id)])))

(fact "HTML is not indented"
  [tag     tags
   content (rand-strs "ABCDEF\n")]
  (not (re-find #"[ \t]" (html [tag content]))))

(fact "Boolean true attribute values are rendered as key=\"key\""
  [attr names]
  (= (html [:p {attr true}])
     (str "<p " attr "=\"" attr "\" />")))

(fact "Boolean false or nil attribute values are not displayed"
  [value [false nil]
   attr  names]
  (= (html [:p {attr value}])
     "<p />"))

(fact "Container tags like div always have an explicit closing tag"
  [tag '(div script span h1 style pre textarea)]
  (= (html [tag])
     (html [tag ""])))
