(ns drone-delivery.gen
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [drone-delivery.util :as util])
  (:gen-class))


(def cli-options
  [["-m" "--max-distance <integer>" "Maximum distance (per axis) for a delivery"
    :default 10
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 %) "Must be a number greater than 0"]]
   ["-t" "--time-allocated <integer>" "Maximum time in hours available for delivery"
    :default 16
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 %) "Must be a number greater than 0"]]
   ["-o" "--orders <integer>" "Number of orders per file"
    :default 16
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 1000) "Must be a number greater than 0 and less than 1000"]]
   ["-n" "--number-of-files <integer>" "Number of files to generate"
    :default 1
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 %) "Must be a number greater than 0" ]]
   ["-d" "--dir <OUTPUT-DIR>" "Output dir for generated files"
    :default "resources/generated/"]
   ["-v" nil "verbose output"
    :id :verbosity
    :default 0
    :update-fn inc]
   ["-h" "--help"]])


(defn- generate-coord
  [max-distance]
  (let [x       (int (- (rand (* 2 max-distance))
                        max-distance))
        y       (int (- (rand (* 2 max-distance))
                        max-distance))
        x-cart  (if (>= 0 x)
                  "E"
                  "W")
        y-cart  (if (>= 0 y)
                  "N"
                  "S")]
    (str y-cart (Math/abs y) x-cart (Math/abs x))))


(def default-start-time-s
  (* 6 60 60))

(defn- convert-time-format
  "Convert seconds to output format"
  [time-s]
  (util/secs-to-order-time (+ time-s default-start-time-s)))

(defn- gen-order-time
  [max-time-s]
  (+ (int (rand max-time-s))
     default-start-time-s))

(defn- generate-order
  [max-distance max-time-s]
  {:coord (generate-coord max-distance)
   :time  (gen-order-time max-time-s)})

(defn- generate-sorted-orders
  [max-distance max-time-s order-count]
  (let [orders          (->> #(generate-order max-distance max-time-s)
                             repeatedly
                             (take order-count))
        sorted-orders   (sort-by :time orders)]
    sorted-orders))

(defn- order-as-output-str
  [order-number order]
  (str (util/order-number-as-output order-number)
       " "
       (:coord order)
       " "
       (util/secs-to-order-time (:time order))))

(defn- generate-order-output
  [orders]
  (map-indexed (fn [idx itm]
                 (order-as-output-str (inc idx) itm))
               orders))

(defn- generate-filename
  [file-prefix file-number]
  (str file-prefix
       (format "%03d" file-number)
       ".input"))



(defn- generate-files
  "Generate ordered files with specified boundaries"
  [max-distance max-time-s order-count output-dir number-of-files verbose]
  (let  [files    (map #(generate-filename "generated" %)
                       (range 1 (inc number-of-files)))]
    (for [file  files]
      (let [orders   (generate-sorted-orders max-distance max-time-s order-count)
            output   (generate-order-output orders)
            filename (str output-dir "/" file)
            _        (if verbose (println (str "Generating file " filename)) nil)
            _        (io/make-parents filename)]
        (spit filename (clojure.string/join \newline output))))))


(defn -main
  [& args]
  (let [opts (parse-opts args cli-options)]
    (cond
      (-> opts :options :help)           (println (:summary opts))
      (-> opts :errors)                  (println (:errors opts))
      :else
      (let [max-distance      (-> opts :options :max-distance)
            max-time-s        (* 60 60
                                 (-> opts :options :time-allocated))
            output-dir        (-> opts :options :dir)
            order-count       (-> opts :options :orders)
            number-of-files   (-> opts :options :number-of-files)
            verbose           (= 1 (-> opts :options :verbosity))]
        (doall (generate-files max-distance
                                       max-time-s
                                       order-count
                                       output-dir
                                       number-of-files
                                       verbose))))))
