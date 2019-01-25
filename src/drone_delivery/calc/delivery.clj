(ns drone-delivery.calc.delivery
  (:require [clojure.tools.logging :as log]
            [drone-delivery.util :as util]))

; Time to travel one unit
(def minutes-per-unit-distance 1)

(defmulti distance-metric
  "Distance measurement for specific metric to a coord"
  (fn [name coord] name))

(defmethod distance-metric "euclidean"
  [_ coord]
  (let [x (nth coord 0)
        y (nth coord 1)]
    (Math/sqrt (+ (Math/pow x 2)
                  (Math/pow y 2)))))

(defmethod distance-metric "edge-only"
  [_ coord]
  (let [x (nth coord 0)
        y (nth coord 1)]
    (+ (Math/abs x) (Math/abs y))))


(defn time-to-deliver-m
  "Time to delivery in minutes, based on the specified coordinates and measurement function"
  [distance-fn coordinate]
  (* minutes-per-unit-distance
     (distance-fn coordinate)))


(defn time-to-deliver-s
  "Time to delivery in seconds, based on the specified coordinates and measurement function"
  [distance-fn coordinate]
  (* 60 (time-to-deliver-m distance-fn coordinate)))

(defn time-to-deliver-order-s
  "Time to delivery in seconds for a specified order and measurement function"
  [distance-fn order]
  (time-to-deliver-s distance-fn (:coords order)))
