(ns drone-delivery.calc.score-test
  (:require [clojure.test :refer :all]
            [drone-delivery.calc.score :refer :all]
            [drone-delivery.util :refer :all]))


(deftest ^:unit simple-score-test
  (let [scorer default-hourly-scorer]
    (testing "Promoter base cases"
      (is (= :promoter
             (scorer (hour-to-sec 1))))
      (is (= :promoter
             (scorer (hour-to-sec 0))))
      (is (= :promoter
             (scorer (hour-to-sec -1)))))
    (testing "Promoter edge cases"
      (is (= :promoter
             (scorer (hour-to-sec 1.9)))))

    (testing "Neutral base cases"
      (is (= :neutral
             (scorer (hour-to-sec 3))))
      (is (= :neutral
             (scorer (hour-to-sec 3.9)))))
    (testing "Neutral edge cases"
      (is (= :neutral
             (scorer (hour-to-sec 2)))))

    (testing "Detractor base cases"
      (is (= :detractor
             (scorer (hour-to-sec 5))))
      (is (= :detractor
             (scorer (hour-to-sec 9))))
      (is (= :detractor
             (scorer (hour-to-sec 20)))))
    (testing "Detractor edge cases"
      (is (= :detractor
             (scorer (hour-to-sec 4)))))))

(defn in-secs [hours] (map hour-to-sec hours))

(deftest ^:unit calc-nps-test
  (let [delivery-times-perfect  (in-secs '(1))              ; -1
        delivery-times-worst    (in-secs '(10))             ;  1
        delivery-times-mixed    (in-secs '(1 3 7))          ;  0
        delivery-times-a        (in-secs '(1 1.5 2 3 4))    ;  .20
        delivery-times-b        (in-secs '(1 10 2 3 4))     ; -.20
        delivery-times-c        (in-secs '(-8 1 0 2 4))     ;  .40
        calc                    (partial calc-nps-with-scorer default-hourly-scorer)]
    (testing "perfect delivery"
      (is (= 1 (calc delivery-times-perfect))))
    (testing "worst delivery"
      (is (= -1 (calc delivery-times-worst))))
    (testing "mixed delivery"
      (is (= 0 (calc delivery-times-mixed))))
    (testing "positive delivery"
      (is (= (/ 1 5) (calc delivery-times-a))))
    (testing "negative delivery"
      (is (= (/ -1 5) (calc delivery-times-b))))
    (testing "edge case delivery"
      (is (= (/ 2 5) (calc delivery-times-c))))))
