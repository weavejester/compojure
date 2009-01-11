(ns test.compojure.html
  (:use fact
        compojure.html))

(def names
  '(foo Bar foo-bar foo_bar fooBar foo:bar))

(fact "The first element of a tag vector defines the tag name"
  [tag names]
  (= (xml [tag "Lorem Ipsum"])
     (str "<" tag ">Lorem Ipsum</" tag ">")))

(fact "Tag vectors can be empty"
  [tag names]
  (= (xml [tag])
     (str "<" tag " />")))

(fact "Tags can be specified by keywords, symbols or strings"
  [tag [:xml 'xml "xml"]]
  (= (xml [tag "Lorem Ipsum"])
     "<xml>Lorem Ipsum</xml>"))

(fact "Tag vectors can contain strings"
  [content (rand-strs)]
   (= (xml [:xml content])
      (str "<xml>" content "</xml>")))

(fact "Tag vectors concatenate their contents"
  [contents (rand-seqs #(rand-strs) 1 100)]
  (= (xml (apply vector :xml contents))
     (str "<xml>" (apply str contents) "</xml>")))

(fact "Sequences in tag vectors are expanded out"
  [contents (rand-seqs #(rand-strs))]
  (= (xml (apply vector :xml contents))
     (xml [:xml contents])))

(fact "Tag vectors can be nested"
  [[dom out]
     {[:a [:b]]          "<a><b /></a>"
      [:a "b" [:c] "d"]  "<a>b<c />d</a>"
      [:a [:b "c"]]      "<a><b>c</b></a>"
      [:a [:b [:c "d"]]] "<a><b><c>d</c></b></a>"}]
  (= (xml dom) out))

(fact "Tag vectors can have attribute maps"
  [[attr-map attr-str]
     {{:a "1"}               "a=\"1\""
      {:b "1" :a "2"}        "a=\"2\" b=\"1\""
      {:b "2" :c "3" :a "1"} "a=\"1\" b=\"2\" c=\"3\""
      {:aBc "d"}             "aBc=\"d\""
      {:a_b "c"}             "a_b=\"c\""
      {:a:b "c"}             "a:b=\"c\""}]
  (= (xml [:xml attr-map "Lorem Ipsum"])
     (str "<xml " attr-str ">Lorem Ipsum</xml>")))

(fact "Special characters are escaped in attribute values"
  [[original escaped]
     {"\"" "&quot;"
      "<"  "&lt;"
      ">"  "&gt;"
      "&"  "&amp;"}]
  (= (xml [:div {:id original}])
     (str "<div id=\"" escaped "\" />")))

(fact "Strings, keywords and symbols can be keys in attribute maps"
  [attr [:id 'id "id"]]
  (= (xml [:span {attr "a"}])
     "<span id=\"a\" />"))

(fact "An attribute map can have keys of different types"
  []
  (= (xml [:span {:a "1" 'b "2" "c" "3"}])
     "<span a=\"1\" b=\"2\" c=\"3\" />"))

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
   content (rand-strs)]
  (= (html [tag content])
     (xml  [tag content])))

(fact "Boolean true attribute values are rendered as key=\"key\""
  [attr names]
  (= (html [:p {attr true}])
     (str "<p " attr "=\"" attr "\" />")))

(fact "Boolean false or nil attribute values are not displayed"
  [value [false nil]
   attr  names]
  (= (html [:p {attr value}])
     "<p />"))

(fact "Options in select lists can have different text and values"
  []
  (= (select-options [["a" "1"] ["b" "2"] ["c" "3"]])
    '([:option {:value "1"} "a"]
      [:option {:value "2"} "b"]
      [:option {:value "3"} "c"])))
