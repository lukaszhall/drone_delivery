(ns drone-delivery.core-test
  (:require [clojure.test :refer :all]
            [drone-delivery.core :refer :all]
            [drone-delivery.calc.score :as score]
            [drone-delivery.calc.delivery :as delivery]
            [drone-delivery.data.order :as order]
            [drone-delivery.io.writer :as writer]
            [drone-delivery.io.reader :as reader]
            [drone-delivery.strat.solver :as solve]))

(def sample-input-filename "resources/integration-test.input")
(def sample-output-filename "resources/integration-test.output")
(def sample-input "WM001 N11W5 05:11:50\nWM002 S3E2 05:11:55\nWM003 N7E50 05:31:50\nWM004 N11E5 06:11:50\n")
(def sample-output "WM001 06:00:00\nWM002 06:24:10\nWM003 06:31:24\nWM004 08:12:24\nNPS 75")


(deftest ^:integration test-find-solution
  (let [_        (spit sample-input-filename sample-input)
        _        (find-solution "identity"
                                "euclidean"
                                sample-input-filename
                                sample-output-filename
                                score/default-hourly-scorer
                                false)
        output   (slurp sample-output-filename)]

    (testing "find-solution with identity solver"
      (is (= output sample-output)))))
