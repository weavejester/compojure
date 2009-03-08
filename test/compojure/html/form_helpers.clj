(ns test.compojure.html.form-helpers
  (:use fact.core)
  (:use compojure.html.form-helpers))

(fact "Options in select lists can have different text and values"
  []
  (= (select-options [["a" "1"] ["b" "2"] ["c" "3"]])
    '([:option {:value "1"} "a"]
      [:option {:value "2"} "b"]
      [:option {:value "3"} "c"])))
