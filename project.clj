(defproject cljx-sampling "0.1.0"
  :description "Consistent sampling and random numbers for Clojure & ClojureScript"
  :url "https://github.com/ashenfad/cljx-sampling"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]
  :source-paths ["src/cljx"]
  :java-source-paths ["src/java"]
  :test-paths ["target/test-classes"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2511"]]
  :auto-clean false
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}

                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}

                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :clj}

                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :cljs}]}

  :cljsbuild {:test-commands {"node" ["node" :node-runner "target/testable.js"]}
              :builds [{:source-paths ["target/classes" "target/test-classes"]
                        :compiler {:output-to "target/testable.js"
                                   :optimizations :advanced
                                   :pretty-print true}}]}

  :profiles {:dev {:plugins [[com.cemerick/clojurescript.test "0.3.3"]
                             [com.keminglabs/cljx "0.5.0"]
                             [com.cemerick/austin "0.1.5"]
                             [lein-cljsbuild "1.0.4-SNAPSHOT"]
                             [jonase/eastwood "0.1.4"]]
                   :prep-tasks [["cljx" "once"] "javac" "compile"]
                   :auto-clean false
                   :aliases {"cleantest" ["do" "clean," "cljx" "once," "test,"
                                          "cljsbuild" "test"]
                             "deploy" ["do" "clean," "cljx" "once," "deploy" "clojars"]
                             "lint" ["do" "check," "eastwood"]
                             "distcheck" ["do" "clean," "lint," "test"]}}})
