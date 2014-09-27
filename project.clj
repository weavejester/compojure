(defproject compojure "1.1.9"
  :description "A concise routing library for Ring"
  :url "https://github.com/weavejester/compojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.macro "0.1.5"]
                 [clout "2.0.0-SNAPSHOT"]
                 [ring/ring-core "1.3.1"]
                 [ring/ring-codec "1.0.0"]]
  :plugins [[codox "0.8.10"]]
  :codox {:src-dir-uri "http://github.com/weavejester/compojure/blob/1.1.9/"
          :src-linenum-anchor-prefix "L"}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]
                        [javax.servlet/servlet-api "2.5"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}})
