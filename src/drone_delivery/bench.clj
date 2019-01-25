(ns drone-delivery.bench
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [drone-delivery.core :as core]
            [drone-delivery.calc.score :as score]
            [drone-delivery.calc.delivery :as delivery]
            [drone-delivery.data.order :as order]
            [drone-delivery.io.writer :as writer]
            [drone-delivery.io.reader :as reader]
            [drone-delivery.strat.solver :as solve])
  (:gen-class))


(def cli-options
  [["-h" "--help"]
   ["-s" "--solvers <command delimited SOLVERS>" (str "Solver Strategies : "
                                                      (-> solve/delivery-solver methods keys))
    :default "identity,permuted,greedy-permuted"]
   ["-t" "--travel <TRAVEL>" (str "Travel Type : "
                                  (-> delivery/distance-metric methods keys))
     :default "euclidean"]
   ["-d" "--dir <DIR>" "input/output dir for input/generated files"
    :default "resources/generated/"]
   ["-v" nil "verbose output"
    :id :verbosity
    :default 0
    :update-fn inc]
   ])


(defn output-filename-from-input
  [solver-name input-filename]
  (let [prefix  (-> input-filename
                    (clojure.string/split #"\.")
                    drop-last
                    clojure.string/join)]
    (str prefix
         "."
         solver-name
         ".output")))


(defn find-solutions-for-files
  "Find multiple input files through core/find-solution"
  [solver-name distance-fn-name dir scorer verbose]
  (let  [files            (file-seq (io/file dir))
         input-files      (filter #(and (.isFile %)
                                        (clojure.string/ends-with? (.getName %) ".input"))
                                  files)
         input-filenames  (->> input-files
                               (map (fn [file] (.getName file)))
                               (map #(str dir "/" %)))
         sorted-files     (sort-by str input-filenames)
         scores           (doall
                           (for [input-file sorted-files
                                 :let [_  (if verbose (println "Processing " input-file))]]
                             (core/find-solution solver-name
                                                 distance-fn-name
                                                 input-file
                                                 (output-filename-from-input solver-name input-file)
                                                 scorer
                                                 false)))
         avg-score        (/ (reduce + scores)
                             (count scores))
         _                (println "Average score " (float avg-score))]
    nil))


(defn -main
  [& args]
  (let [opts (parse-opts args cli-options)]
    (cond
      ;(= 0 (count (-> opts :arguments))) (println (:summary opts))
      (-> opts :options :help)           (println (:summary opts))
      (-> opts :errors)                  (println (:errors opts))
      :else
      (let [;_ (clojure.pprint/pprint opts)
            solver-names      (clojure.string/split (-> opts :options :solvers) #",")
            distance-fn-name  (-> opts :options :travel)
            dir               (-> opts :options :dir)
            scorer            score/default-hourly-scorer
            verbose           (= 1 (-> opts :options :verbosity))]

        (doall (for [solver-name solver-names
                     :let [_  (println "\n  ---===== Solver '" solver-name "' =====---")
                           _  (time (find-solutions-for-files solver-name
                                                              distance-fn-name
                                                              dir
                                                              scorer
                                                              verbose))
                           ;_  (println "-------------------------------------")
                           ]]
                 nil
                 ))))))
