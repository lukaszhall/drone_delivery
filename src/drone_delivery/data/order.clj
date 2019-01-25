(ns drone-delivery.data.order
  (:require [clojure.tools.logging :as log]
            [drone-delivery.util :as util]
            [drone-delivery.calc.delivery :as delivery]))

(defn normalize-input-order
  "Convert file input format into order map"
  [order]
    {:order    (nth order 0)
     :coords   (util/coord-to-cartesian (nth order 1))
     :time     (util/order-time-to-secs (nth order 2))})

(defn normalize-input-orders
  "Convert file input format into vector of order maps"
  [orders]
  (map normalize-input-order orders))
