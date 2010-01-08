(ns compojure.html.page-helpers-test
  (:use compojure.html.page-helpers
        clojure.contrib.test-is))

(deftest test-doctype
  (testing "html4"
    (is (= (doctype :html4)
           (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" "
                "\"http://www.w3.org/TR/html4/strict.dtd\">\n"))))
  (testing "xhtml-strict"
    (is (= (doctype :xhtml-strict)
           (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
              "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"))))
  (testing "html5"
    (is (= (doctype :html5)
           (str "<!DOCTYPE html>"))))
  (testing "xhtml-transitional"
    (is (= (doctype :xhtml-transitional)
           (str  "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
                 "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n")))))

(deftest test-xhtml-tag
  (is (= (xhtml-tag "test")
         [:html {:xmlns "http://www.w3.org/1999/xhtml", "xml:lang" "test", :lang "test"} nil])))

(deftest test-include-js
  (testing "one"
    (is (= (include-js "foo.js")
           '([:script {:type "text/javascript", :src "foo.js"}]))))
  (testing "many"
    (is (= (include-js "foo.js" "bar.js" "baz.js")
           '([:script {:type "text/javascript", :src "foo.js"}]
             [:script {:type "text/javascript", :src "bar.js"}]
             [:script {:type "text/javascript", :src "baz.js"}])))))

(deftest test-include-css
  (testing "one"
    (is (= (include-css "foo.css")
           '([:link {:type "text/css" :href "foo.css" :rel "stylesheet"}]))))
  (testing "many"
    (is (= (include-css "foo.css" "bar.css" "baz.css")
           '([:link {:type "text/css", :href "foo.css", :rel "stylesheet"}]
             [:link {:type "text/css", :href "bar.css", :rel "stylesheet"}]
             [:link {:type "text/css", :href "baz.css", :rel "stylesheet"}])))))

(deftest test-javascript-tag
  (is (= (javascript-tag "alert('hi');")
         [:script {:type "text/javascript"}
          (str "//<![CDATA[\n" "alert('hi');" "\n//]]>")])))

(deftest test-link-to
  (is (= (link-to "http://compojure.org")
         [:a {:href "http://compojure.org"} nil])))

(deftest test-url-encode
  (is (= (url-encode "foo&bar*/baz.net")
         (str "foo%26bar*%2Fbaz.net"))))

(deftest test-url-params
  (is (= (url-params "http://example.com" {:lang "en", :offset 10})
         "http://example.com?lang=en&offset=10")))

(deftest test-unordered-list
  (is (= (unordered-list ["a" "b"])
         [:ul {}
          '([:li "a"] [:li "b"])])))

(deftest test-ordered-list
  (is (= (ordered-list ["b" "a"])
         [:ol {}
          '([:li "b"] [:li "a"])])))