(defproject ingred "0.1.0-SNAPSHOT"
  :description "Search recipes by ingredient"
  :url "https://github.com/oliyh/ingred"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.2"]
                 [ring "1.2.0"]
                 [ring-server "0.2.8" :exclusions [[org.clojure/clojure]
                                                   [ring]]]
                 [ring/ring-json "0.2.0"]
                 [compojure "1.1.5" :exclusions [[org.clojure/clojure] [ring/ring-core]]]
                 [cheshire "5.2.0"]])
