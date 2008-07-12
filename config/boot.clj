(load-file "lib/lib.clj")
(lib/use compojure)
(lib/use glob)

; Load up all the modules
(doseq module (glob "modules/*")
  (lib/load-libs :require
    (symbol (str module "/init"))))

; Load up any .clj file in the app directory
(doseq file (glob "app/**.clj")
  (load-file (str file)))
