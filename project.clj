(defproject compojure "0.6.2"
  :description "A concise web framework for Clojure"
  :url "http://github/weavejester/compojure"
  :dependencies
    [[org.clojure/clojure "1.2.0"]
     [org.clojure/clojure-contrib "1.2.0"]
     [clout "0.4.1"]
     [ring/ring-core "0.3.7"]]
  :dev-dependencies
    [[ring-mock "0.1.1"]
     [autodoc "0.7.1" :exclusions [org.clojure/clojure
                                   org.clojure/clojure-contrib]]]
  :autodoc
    {:name "Compojure"
     :description "A concise web framework for Clojure"
     :copyright "Copyright 2011 James Reeves"
     :root "."
     :source-path "src"
     :web-src-dir "http://github.com/weavejester/compojure/blob"
     :web-home "http://weavejester.github.com/compojure"
     :output-path "autodoc"
     :namespaces-to-document ["compojure"]
     :load-except-list [#"/test/" #"project.clj"]})
