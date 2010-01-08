(ns compojure.html.page-helpers-test
  (:use compojure.html.page-helpers
        clojure.contrib.test-is))

(deftest test-doctype-html4
  (is (= (doctype :html4)
         (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" "
              "\"http://www.w3.org/TR/html4/strict.dtd\">\n"))))

(deftest test-doctype-xhtml-strict
  (is (= (doctype :xhtml-strict)
         (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
              "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"))))

(deftest test-doctype-html5
	(is (= (doctype :html5)
			  (str "<!DOCTYPE html>"))))

(deftest test-doctype-xhtml-transitional
  (is (= (doctype :xhtml-transitional)
         (str  "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
               "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"))))

(deftest test-xhtml-tag
  (is (= (xhtml-tag "test")
         [:html {:xmlns "http://www.w3.org/1999/xhtml", "xml:lang" "test", :lang "test"} nil])))

(deftest test-include-js
  (is (= (include-js "foo.js")
         '([:script {:type "text/javascript", :src "foo.js"}]))))

(deftest test-include-css
  (is (= (include-css "foo.css")
         '([:link {:type "text/css" :href "foo.css" :rel "stylesheet"}]))))

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