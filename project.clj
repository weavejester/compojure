(defproject compojure "1.3.0"
  :description "A concise routing library for Ring"
  :url "https://github.com/weavejester/compojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.macro "0.1.5"]
                 [clout "2.1.0"]
                 [medley "0.5.3"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-codec "1.0.0"]]
  :plugins [[codox "0.8.10"]]
  :codox {:src-dir-uri "http://github.com/weavejester/compojure/blob/1.3.0/"
          :src-linenum-anchor-prefix "L"}
  :profiles
  {:dev {:jvm-opts ^:replace []
         :dependencies [[ring-mock "0.1.5"]
                        [criterium "0.4.3"]
                        [javax.servlet/servlet-api "2.5"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
   :1.7 {:dependencies [[org.clojure/clojure "1.7.0-alpha2"]]}})
