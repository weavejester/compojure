(use 'fact)
(use 'test.compojure.html)

(.println *test-out* "Facts:")
(print-results (verify-facts 'test.compojure.html))
