(ns test.compojure.html
  (:use fact
        (compojure html)))

(def names
  '(foo Bar foo-bar foo_bar fooBar))

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

(def test-contents
  ["Lorem"
   "Lorem Ipsum"
   ""
   "Lorem\nIpsum"
   "  Lorem"
   "Ipsum  "
   "  Lorem\n    Ipsum"
   "Lorem<br />Ipsum"])

(fact "Tag vectors can contain strings"
  [content test-contents]
  (= (xml [:xml content])
     (str "<xml>" content "</xml>")))

(def test-seqs
  ['("a" "b")
   '("a" " b")
   '("a" "\n" "b")
   '("a " "b " "c " "d")])

(fact "Tag vectors concatenate their contents"
  [contents test-seqs]
  (= (xml (apply vector :xml contents))
     (str "<xml>" (apply str contents) "</xml>")))

(fact "Sequences in tag vectors are expanded out"
  [contents test-seqs]
  (= (xml (apply vector :xml contents))
     (xml [:xml contents])))

(fact "Tag vectors can be nested"
  [dom [[:a [:b]]
        [:a "b" [:c] "d"]
        [:a [:b "c"]]
        [:a [:b [:c "d"]]]]
   out ["<a><b /></a>"
        "<a>b<c />d</a>"
        "<a><b>c</b></a>"
        "<a><b><c>d</c></b></a>"]]
  (= (xml dom) out))

(fact "Tag vectors can have attribute maps"
  [attr-map [{:a "1"}
             {:b "1" :a "2"}
             {:b "2" :c "3" :a "1"}
             {:aBc "d"}
             {:a_b "c"}
             {:a:b "c"}
             {:something_rather_long "some text"}]
   attr-str ["a=\"1\""
             "a=\"2\" b=\"1\""
             "a=\"1\" b=\"2\" c=\"3\""
             "aBc=\"d\""
             "a_b=\"c\""
             "a:b=\"c\""
             "something_rather_long=\"some text\""]]
  (= (xml [:xml attr-map "Lorem Ipsum"])
     (str "<xml " attr-str ">Lorem Ipsum</xml>")))

(fact "Special characters are escaped in attribute values"
  [char    ["\"" "<" ">" "&"]
   escaped ["&quot;" "&lt;" "&gt;" "&amp;"]]
  (= (xml [:div {:id char}])
     (str "<div id=\"" escaped "\" />")))

(def inline-tags
  '(a span em strong code img))

(fact "HTML tag vectors have syntax sugar for class attributes"
  [tag   inline-tags
   class names]
  (= (html [tag {:class class}])
     (html [(str tag "." class)])))

(fact "HTML tag vectors have syntax sugar for id attributes"
  [tag inline-tags
   id  names]
  (= (html [tag {:id id}])
     (html [(str tag "#" id)])))

(fact "The contents of HTML block tags are indented")

(fact "The HTML pre tag is rendered without indentation"
  [content test-contents]
  (= (html [:body [:div [:pre content]]])
     "<body>\n  <div>\n    <pre>" content "</pre>\n  </div>\n</body>\n"))
