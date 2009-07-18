(ns test.compojure.html.gen
  (:use compojure.html.gen)
  (:use clojure.contrib.test-is))

(deftest tag-text
  (is (= (html [:text "Lorem Ipsum"]) "<text>Lorem Ipsum</text>")))

(deftest empty-tags
  (is (= (html [:text]) "<text />")))

(deftest empty-block-tags
  (is (= (html [:div]) "<div></div>"))
  (is (= (html [:h1]) "<h1></h1>"))
  (is (= (html [:script]) "<script></script>")))

(deftest empty-links-tag
  (is (= (html [:a]) "<a></a>")))

(deftest tags-can-be-strs
  (is (= (html ["div"] "<div></div>"))))

(deftest tags-can-be-symbols
  (is (= (html ['div] "<div></div>"))))

(deftest tag-concatenation
  (is (= (html [:body "foo" "bar"]) "<body>foobar</body>"))
  (is (= (html [:body [:p] [:br]])) "<body><p /><br /></body>"))

(deftest tag-seq-expand
  (is (= (html [:body (list "foo" "bar")])
         "<body>foobar</body>")))

(deftest html-seq-expand
  (is (= (html (list [:p "a"] [:p "b"]))
         "<p>a</p><p>b</p>")))

(deftest nested-tags
  (is (= (html [:div [:p]] "<div><p /></div>")))
  (is (= (html [:div [:b]] "<div><b></b></div>")))
  (is (= (html [:p [:span [:a "foo"]]])
         "<p><span><a>foo</a></span></p>")))

(deftest attribute-maps
  (is (= (html [:xml {:a "1", :b "2"}])
         "<xml a=\"1\" b=\"2\" />")))

(deftest blank-attribute-map
  (is (= (html [:xml {}]) "<xml />")))

(deftest escaped-chars
  (is (= (escape-html "\"") "&quot;"))
  (is (= (escape-html "<") "&lt;"))
  (is (= (escape-html ">") "&gt;"))
  (is (= (escape-html "&") "&amp;")))

(deftest escaped-attrs
  (is (= (html [:div {:id "\""}])
         "<div id=\"&quot;\"></div>")))

(deftest attrs-can-be-strs
  (is (= (html [:img {"id" "foo"}]) "<img id=\"foo\" />")))

(deftest attrs-can-be-symbols
  (is (= (html [:img {'id "foo"}]) "<img id=\"foo\" />")))

(deftest attr-keys-different-types
  (is (= (html [:xml {:a "1", 'b "2", "c" "3"}])
         "<xml a=\"1\" b=\"2\" c=\"3\" />")))

(deftest tag-class-sugar
  (is (= (html [:div.foo]) "<div class=\"foo\"></div>")))

(deftest tag-many-class-sugar
  (is (= (html [:div.a.b]) "<div class=\"a b\"></div>"))
  (is (= (html [:div.a.b.c]) "<div class=\"a b c\"></div>")))

(deftest tag-id-sugar
  (is (= (html [:div#foo]) "<div id=\"foo\"></div>")))

(deftest tag-id-and-classes
  (is (= (html [:div#foo.bar.baz])
         "<div class=\"bar baz\" id=\"foo\"></div>")))

(deftest html-not-indented
  (is (= (html [:p "Lorem\nIpsum"]) "<p>Lorem\nIpsum</p>")))

(deftest attrs-bool-true
  (is (= (html [:input {:type "checkbox" :checked true}])
         "<input checked=\"checked\" type=\"checkbox\" />")))

(deftest attrs-bool-false
  (is (= (html [:input {:type "checkbox" :checked false}])
         "<input type=\"checkbox\" />")))
