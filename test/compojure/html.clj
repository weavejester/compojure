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

(fact "Strings, keywords and symbols can be keys in attribute maps"
  [attr [:id 'id "id"]]
  (= (xml [:span {attr "a"}])
     "<span id=\"a\" />"))

(fact "An attribute map can have keys of different types"
  []
  (= (xml [:span {:a "1" 'b "2" "c" "3"}])
     "<span a=\"1\" b=\"2\" c=\"3\" />"))

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

(fact "The content of HTML 'block' tags is indented"
  [tag '(body div p blockquote script)]
  (= (html [tag "Lorem\nIpsum"])
     (str "<" tag ">\n  Lorem\n  Ipsum\n</" tag ">\n")))

(fact "A newline character is appended to HTML 'line' tags"
  [tag '(h1 h3 title li)]
  (= (html [tag "Lorem Ipsum"])
     (str "<" tag ">Lorem Ipsum</" tag ">\n")))

(fact "Nested tags result in nested indentation"
  [content  test-contents
   indented ["    Lorem"
             "    Lorem Ipsum"
             "    "
             "    Lorem\n    Ipsum"
             "      Lorem"
             "    Ipsum  "
             "      Lorem\n        Ipsum"
             "    Lorem<br />Ipsum"]]
  (= (html [:div [:p content]])
     (str "<div>\n  <p>\n" indented "\n  </p>\n</div>\n")))

(fact "The HTML pre tag is always rendered without indentation"
  [content test-contents]
  (= (html [:body [:div [:pre content]]])
     "<body>\n  <div>\n    <pre>" content "</pre>\n  </div>\n</body>\n"))

(fact "Options in select lists can have different text and values"
  []
  (= (select-options [["a" "1"] ["b" "2"] ["c" "3"]])
    '([:option {:value "1"} "a"]
      [:option {:value "2"} "b"]
      [:option {:value "3"} "c"])))
