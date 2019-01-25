(ns drone-delivery.io.reader
  (:require [clojure.tools.logging :as log]))


(def line-regex #"(WM\d{3}) ([NS]\d+[EW]\d+) (\d{2}:\d{2}:\d{2}).*")

(defn parse-delivery
  "Parse into [order, location, time] or nil"
  [line]
  (let [match (re-matches line-regex line)]
    (if (nil? match)
      (log/error "Failed to parse line '" line "'")
      (rest match))))

(defn delivery?
  "true if matches delivery format"
  [line]
  (not (nil? (re-matches line-regex line))))


(defn load-order-file [path]
  (slurp path))

(defn parse-file [file]
  (let [lines       (clojure.string/split-lines file)
        deliveries  (map parse-delivery lines)]
    deliveries))

(defn load-deliveries
  "Read file into sequence of order vectors ['order number', 'coordinate', 'time']"
  [file]
  (-> file
      load-order-file
      parse-file))
