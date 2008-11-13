(use 'fact)
(use 'test.compojure.html)

(.println *test-out* "compojure.html:")
(print-results (verify-facts 'test.compojure.html))
