(use 'fact)
(require 'test.compojure.html)
(require 'test.compojure.http.routes)
(require 'test.compojure.html.form-helpers)
(require 'test.compojure.validation)

(.println *test-out* "compojure.html:")
(print-color-results (verify-facts 'test.compojure.html))
(.println *test-out*)

(.println *test-out* "compojure.html.form-helpers:")
(print-color-results (verify-facts 'test.compojure.html.form-helpers))
(.println *test-out*)

(.println *test-out* "compojure.http.routes:")
(print-color-results (verify-facts 'test.compojure.http.routes))
(.println *test-out*)

(.println *test-out* "compojure.validation:")
(print-color-results (verify-facts 'test.compojure.validation))
