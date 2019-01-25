(ns drone-delivery.util
  (:require [clojure.tools.logging :as log]))


(defn min-to-sec [mins] (* 60 mins))
(defn hour-to-sec [hours] (* 60 60 hours))

(defn score-to-pct [score] (int (* 100)))

(defn order-time-to-secs
  "Convert order time HH:MM:SS format to seconds"
  [order-time]
  (let [parsed-time (re-matches #"(\d{2}):(\d{2}):(\d{2})" order-time)
        hours       (Integer. (nth parsed-time 1))
        mins        (Integer. (nth parsed-time 2))
        secs        (Integer. (nth parsed-time 3))]
    (+ secs
       (min-to-sec mins)
       (hour-to-sec hours))))

(defn order-number-as-output
  "convert integer into file format WM001, etc.."
  [order-no]
  (str "WM"
       (format "%03d" order-no)))

(defn secs-to-order-time
  "Convert absolute seconds to order time format HH:MM:SS"
  [seconds]
  (if (< seconds 0)
    (throw (IllegalArgumentException. "Seconds may not be less than zero"))
    (let [hours    (Math/floor (/ seconds
                                  (* 60 60)))
          mins     (Math/floor (/ (- seconds (* hours 60 60))
                                  60))
          secs     (- seconds
                      (* hours 60 60)
                      (* mins 60))]

      (format "%02d:%02d:%02d" (int hours) (int mins) (int secs)))))


; Coordinate format
(def coord-regex #"([NS])(\d+)([EW])(\d+)")

(defn coord-to-cartesian
  "Convert town coordinates to cartesian"
  [coord]
  (let [matches  (rest (re-matches coord-regex coord))
        x-abs    (Integer. (nth matches 1))
        y-abs    (Integer. (nth matches 3))
        y        (if (= (nth matches 0) "N")
                   x-abs
                   (unchecked-negate x-abs))
        x        (if (= (nth matches 2) "E")
                   y-abs
                   (unchecked-negate y-abs))]
    [x y]))
