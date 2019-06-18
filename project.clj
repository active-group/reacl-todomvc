(defproject todomvc "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [reacl "2.2.0-SNAPSHOT"]
                 [secretary "1.2.2"]
                 [alandipert/storage-atom "1.2.4"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :profiles {:dev {:cljsbuild {:builds [{:source-paths ["src"]
                                         :compiler {:output-to "js/app.js"}}]}}

             :prod {:cljsbuild {:builds [{:source-paths ["src"]
                                          :compiler {:output-to "js/app.js"
                                                     :optimizations :advanced
                                                     :elide-asserts true
                                                     :pseudo-names true
                                                     :pretty-print true}}]}}})
