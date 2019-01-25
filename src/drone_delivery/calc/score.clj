(ns drone-delivery.calc.score
  (:require [clojure.tools.logging :as log]
            [drone-delivery.util :as util]))


(defn calc-nps
  "Calculate NPS based on supplied scorer"
  [ratings]
  (let [freqs         (merge {:promoter 0 :neutral 0 :detractor 0}
                             (frequencies ratings))
        total-cnt     (reduce + (vals freqs))
        promoter-cnt  (:promoter freqs)
        detractor-cnt (:detractor freqs)]

    (- (/ promoter-cnt total-cnt)
       (/ detractor-cnt total-cnt))))


(defn calc-nps-with-scorer
  "Calculate NPS based on supplied scorer"
  [scorer delivery-times]
  (let [ratings       (map scorer delivery-times)]
    (calc-nps ratings)))

(defn create-simple-scorer
  "Generate closure returning <low Promotor;  >=low,<high Neutral; >=high Detractor"
  [low high]
  (fn [time]
    (cond
      (< time low)                      :promoter
      (and (>= time low)
           (< time high))               :neutral
      :else                             :detractor)))

(def default-hourly-scorer
  (create-simple-scorer
   (util/hour-to-sec 2)
   (util/hour-to-sec 4)))


(defn calc-solution-nps
  "For a solution, calculate NPS"
  [scorer solution]
  (let  [delivery-times (map #(- (:delivery-time %)
                                 (-> % :order :time))
                             solution)]
    (* 100 (calc-nps-with-scorer scorer delivery-times))))

(defn extract-solution-nps
  "For a solution with embedded ratings, extract a non-integer NPS via :rating"
  [solution]
  (* 100 (calc-nps (map :rating solution))))
