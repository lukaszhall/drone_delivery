(ns drone-delivery.util-test
  (:require [clojure.test :refer :all]
            [drone-delivery.util :refer :all]))

(deftest ^:unit order-time-to-secs-test
  (testing "Time conversion to seconds"
    (is (== 0
            (order-time-to-secs "00:00:00")))
    (is (== 30
            (order-time-to-secs "00:00:30")))
    (is (== (+ 30 60)
            (order-time-to-secs "00:01:30")))
    (is (== (* 5 60)
            (order-time-to-secs "00:05:00")))
    (is (== (* 13 60 60)
            (order-time-to-secs "13:00:00")))
    (is (== (+ (* 2 60 60) (* 2 60) 2)
            (order-time-to-secs "02:02:02"))))
  (testing "Time conversion failure"
    (is (thrown? Exception (order-time-to-secs "0:00:00")))))


(deftest ^:unit secs-to-order-time-test
  (testing "Time conversion to order format"
    (is (= (secs-to-order-time 0)
           "00:00:00"))
    (is (= (secs-to-order-time 30)
           "00:00:30"))
    (is (= (secs-to-order-time (+ 30 60))
           "00:01:30"))
    (is (= (secs-to-order-time (* 5 60))
           "00:05:00"))
    (is (= (secs-to-order-time (* 13 60 60))
           "13:00:00"))
    (is (= (secs-to-order-time (+ (* 2 60 60) (* 2 60) 2))
           "02:02:02")))
  (testing "Negative Time conversion failure"
    (is (thrown? IllegalArgumentException (secs-to-order-time -3)))))

(deftest ^:unit coord-to-cart-test
  (testing "Negation test"
    (is (= [-5 -11] (coord-to-cartesian "S11W5"))))
  (testing "Positive test"
    (is (= [5 11] (coord-to-cartesian "N11E5"))))
  (testing "Mixed test"
    (is (= [-2 4] (coord-to-cartesian "N4W2"))))
  (testing "Failure test"
    (is (thrown? Exception (coord-to-cartesian "Q11E5")))))
