(use 'fact)
(require '(test.compojure html
                          http
                          validation))

(.println *test-out* "compojure.html:")
(print-results (verify-facts 'test.compojure.html))
(.println *test-out*)

(.println *test-out* "compojure.http:")
(print-results (verify-facts 'test.compojure.http))
(.println *test-out*)

(.println *test-out* "compojure.validation:")
(print-results (verify-facts 'test.compojure.validation))
