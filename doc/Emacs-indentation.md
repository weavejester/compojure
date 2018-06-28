# Emacs Indentation

You can add the following code to your `init.el` file to get better indentation for Compojure macros:

```lisp
;; Place the function call requiring `clojure-mode` after the call to `package-initialize`
;; if you are using Emacs version 24.1.1
(require 'clojure-mode)

(define-clojure-indent
  (defroutes 'defun)
  (GET 2)
  (POST 2)
  (PUT 2)
  (DELETE 2)
  (HEAD 2)
  (ANY 2)
  (OPTIONS 2)
  (PATCH 2)
  (rfn 2)
  (let-routes 1)
  (context 2))
```
