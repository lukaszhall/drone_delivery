(ns drone-delivery.io.writer
  (:require [clojure.tools.logging :as log]
            [drone-delivery.util :as util]
            [drone-delivery.calc.score :as score]))

;; Output format:
;;
;; WM002 06:00:00
;; WM001 06:14:18
;; ...
;; NPS 86


(defn- order-to-output
  "Convert solution element to output"
  [solution-order]
  (let   [order-id     (-> solution-order
                           :order
                           :order)
          start-time   (util/secs-to-order-time (:start-time solution-order))]
    (str order-id " " start-time)))

(defn solution-to-output
  "Convert entire solution to output"
  [solution]
  (let [output-rows  (into [] (map order-to-output solution))
        ;;nps-score    (int (* 100 (score/calc-nps (map :rating solution))))
        nps-score    (int (score/extract-solution-nps solution))
        output       (conj output-rows (str "NPS " nps-score))]
    (clojure.string/join \newline output)))

(defn solution-to-file
  "Output entire solution to file"
  [file solution]
  (let [output (solution-to-output solution)
        _      (spit file output)]
    solution ))
