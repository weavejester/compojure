(defproject compojure "1.5.1"
  :description "A concise routing library for Ring"
  :url "https://github.com/weavejester/compojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.macro "0.1.5"]
                 [clout "2.1.2"]
                 [medley "0.8.2"]
                 [ring/ring-core "1.5.1"]
                 [ring/ring-codec "1.0.1"]]
  :plugins [[lein-codox "0.9.5"]]
  :codox
  {:output-path "codox"
   :metadata {:doc/format :markdown}
   :source-uri "http://github.com/weavejester/compojure/blob/{version}/{filepath}#L{line}"}
  :aliases
  {"test-all" ["with-profile" "default:+1.8" "test"]}
  :profiles
  {:dev {:jvm-opts ^:replace []
         :dependencies [[ring/ring-mock "0.3.0"]
                        [criterium "0.4.3"]
                        [javax.servlet/servlet-api "2.5"]]}
   :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}})
