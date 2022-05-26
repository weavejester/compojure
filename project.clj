(defproject compojure "1.7.0"
  :description "A concise routing library for Ring"
  :url "https://github.com/weavejester/compojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.macro "0.1.5"]
                 [clout "2.2.1"]
                 [medley "1.4.0"]
                 [ring/ring-core "1.9.5"]
                 [ring/ring-codec "1.2.0"]]
  :plugins [[lein-codox "0.10.7"]]
  :codox
  {:output-path "codox"
   :metadata {:doc/format :markdown}
   :source-uri "http://github.com/weavejester/compojure/blob/{version}/{filepath}#L{line}"}
  :aliases
  {"test-all" ["with-profile" "default:+1.8:+1.9:+1.10:+1.11" "test"]}
  :profiles
  {:dev {:jvm-opts ^:replace []
         :dependencies [[ring/ring-mock "0.4.0"]
                        [criterium "0.4.6"]
                        [javax.servlet/servlet-api "2.5"]]}
   :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
   :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.0"]]}
   :1.11 {:dependencies [[org.clojure/clojure "1.11.1"]]}})
