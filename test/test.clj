(use 'fact)
(use 'fact.output.verbose)
(require 'test.compojure.html)
(require 'test.compojure.http.routes)
(require 'test.compojure.html.form-helpers)
(require 'test.compojure.validation)

(println "compojure.html:")
(print-color-results (verify-facts 'test.compojure.html))
(println)

(println "compojure.html.form-helpers:")
(print-color-results (verify-facts 'test.compojure.html.form-helpers))
(println)

(println "compojure.http.routes:")
(print-color-results (verify-facts 'test.compojure.http.routes))
(println)

(println "compojure.validation:")
(print-color-results (verify-facts 'test.compojure.validation))
