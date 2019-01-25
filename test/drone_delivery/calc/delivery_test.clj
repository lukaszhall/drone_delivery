(ns drone-delivery.calc.delivery-test
  (:require [clojure.test :refer :all]
            [clojure.tools.logging :as log]
            [drone-delivery.calc.delivery :refer :all]
            [drone-delivery.util :refer :all]))

(def distance-euclidean (partial distance-metric "euclidean"))
(def distance-edge-only (partial distance-metric "edge-only"))


(deftest ^:unit distance-test
  (testing "Euclidean distance"
    (is (== 5 (distance-euclidean [3 4]))))
  (testing "Edge distance"
    (is (== 7 (distance-edge-only [3 4])))))


(deftest ^:unit time-to-deliver-m-test
  (let  [ttd-euclidean  (partial time-to-deliver-m distance-euclidean)
         ttd-edge       (partial time-to-deliver-m distance-edge-only)]
    (testing "Delivery time SW quadrant euclidean"
      (is (== 5 (ttd-euclidean  [-3 4]))))
    (testing "Delivery time NW quadrant edge"
      (is (== 11 (ttd-edge  [5 6]))))))


(deftest ^:unit time-to-deliver-order-s-test
  (let  [ttdo-euclidean  (partial time-to-deliver-order-s distance-euclidean)]
    (testing "Delivery time SW quadrant euclidean"
      (is (== (min-to-sec 5) (ttdo-euclidean  {:coords [3 4]}))))))
