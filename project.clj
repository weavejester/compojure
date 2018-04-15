(defproject compojure "1.6.1"
  :description "A concise routing library for Ring"
  :url "https://github.com/weavejester/compojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.macro "0.1.5"]
                 [clout "2.2.1"]
                 [medley "1.0.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-codec "1.1.0"]]
  :plugins [[lein-codox "0.10.3"]]
  :codox
  {:output-path "codox"
   :metadata {:doc/format :markdown}
   :source-uri "http://github.com/weavejester/compojure/blob/{version}/{filepath}#L{line}"}
  :aliases
  {"test-all" ["with-profile" "default:+1.8:+1.9" "test"]}
  :profiles
  {:dev {:jvm-opts ^:replace []
         :dependencies [[ring/ring-mock "0.3.2"]
                        [criterium "0.4.4"]
                        [javax.servlet/servlet-api "2.5"]]}
   :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
   :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}})
