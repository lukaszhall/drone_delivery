(ns drone-delivery.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [drone-delivery.calc.score :as score]
            [drone-delivery.calc.delivery :as delivery]
            [drone-delivery.data.order :as order]
            [drone-delivery.io.writer :as writer]
            [drone-delivery.io.reader :as reader]
            [drone-delivery.strat.solver :as solve])
  (:gen-class))


(def cli-options
  ;; An option with a required argument
  [["-s" "--solver <SOLVER>" (str "Solver Strategy : "
                                  (-> solve/delivery-solver methods keys))
    :default "permuted"]
   ["-t" "--travel <TRAVEL>" (str "Travel Type : "
                                  (-> delivery/distance-metric methods keys))
     :default "euclidean"]
   ["-o" "--output <OUTPUT-FILE>" "Output file location"
    :default "resources/sample.output"]
   ["-v" nil "verbose output"
    :id :verbosity
    :default 0
    :update-fn inc]
   ["-h" "--help"]])


(defn spy
  [print? context]
  (if print?
    (let [_  (clojure.pprint/pprint context)]
      context)
    context))


(defn find-solution
  "Feed input through requested scoring, solver, and timer into output file"
  [solver-name distance-fn-name input-file-name output-file-name scorer verbose]
  (let [distance-fn  (partial delivery/distance-metric distance-fn-name)
        timer        (partial delivery/time-to-deliver-order-s distance-fn)
        ;;scorer       score/default-hourly-scorer
        solver       (partial solve/delivery-solver solver-name)]

    (->> input-file-name
         reader/load-deliveries
         order/normalize-input-orders
         (solver scorer timer)
         (spy verbose)
         (writer/solution-to-file output-file-name)
         (score/extract-solution-nps)
         (spy verbose))))


(defn -main
  [& args]
  (let [opts (parse-opts args cli-options)]
    (cond
      (= 0 (count (-> opts :arguments))) (println (:summary opts))
      (-> opts :options :help)           (println (:summary opts))
      (-> opts :errors)                  (println (:errors opts))
      :else
      (let [solver            (-> opts :options :solver)
            distance-fn-name  (-> opts :options :travel)
            input-filename    (first (:arguments opts))
            output-filename   (-> opts :options :output)
            scorer            score/default-hourly-scorer
            verbose           (= 1 (-> opts :options :verbosity))]

            (find-solution solver
                           distance-fn-name
                           input-filename
                           output-filename
                           scorer
                           verbose)))))
