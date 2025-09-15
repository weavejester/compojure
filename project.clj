(defproject compojure "1.7.1"
  :description "A concise routing library for Ring"
  :url "https://github.com/weavejester/compojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.macro "0.2.1"]
                 [clout "2.2.1"]
                 [dev.weavejester/medley "1.9.0"]
                 [ring/ring-core "1.15.1"]
                 [ring/ring-codec "1.3.0"]]
  :plugins [[lein-codox "0.10.8"]]
  :codox
  {:output-path "codox"
   :metadata {:doc/format :markdown}
   :source-uri "http://github.com/weavejester/compojure/blob/{version}/{filepath}#L{line}"}
  :aliases
  {"test-all" ["with-profile" "default:+1.10:+1.11:+1.12" "test"]}
  :profiles
  {:dev {:jvm-opts ^:replace []
         :dependencies [[ring/ring-mock "0.6.2"]
                        [criterium "0.4.6"]
                        [javax.servlet/servlet-api "2.5"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.0"]]}
   :1.11 {:dependencies [[org.clojure/clojure "1.11.4"]]}
   :1.12 {:dependencies [[org.clojure/clojure "1.12.2"]]}})
