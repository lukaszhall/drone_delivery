(defproject drone_delivery "0.1.0-SNAPSHOT"
  :description "Drone Delivery implementation"
  :url "TBD"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.clojure/tools.cli "0.4.1"]
                 [fipp "0.6.14"]]
  :main ^:skip-aot drone-delivery.core
  :profiles     {:uberjar {:aot :all}
                 :main-solve {:main drone-delivery.core}
                 :main-gen   {:main drone-delivery.gen}
                 :main-bench {:main drone-delivery.bench}}
  :aliases      {"solve"      ["with-profile" "main-solve" "run"]
                 "generate"   ["with-profile" "main-gen" "run"]
                 "benchmark"  ["with-profile" "main-bench" "run"]}
  :target-path "target/%s"
  )
