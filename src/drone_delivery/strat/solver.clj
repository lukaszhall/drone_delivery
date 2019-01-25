(ns drone-delivery.strat.solver
  (:require [clojure.tools.logging :as log]
            [drone-delivery.calc.delivery :as delivery]
            [drone-delivery.calc.score :as score]
            [drone-delivery.data.order :as order]
            [drone-delivery.util :as util]
            [fipp.clojure :as fipp]
            ))

;; Solution is expressed as a sequence of:
;;  {
;;    order           =>  {normalized-order}
;;    start-time      =>  {order_launch_time_s}
;;    deliver-time    =>  {order_complete_time_s}
;;    completion-time =>  {completion_time_s}
;;    rating          =>  {scored_rating}
;;  }


; drone start at 6am
(def drone-start-time-s (util/hour-to-sec 6))

(defn- add-order-to-solution
  "Append order to solution"
  [scorer-fn timer-fn start-time solution order]
  (let [travel-time     (Math/ceil (timer-fn order))
        delivery-time   (+ start-time travel-time)
        completion-time (+ delivery-time travel-time)
        rating          (scorer-fn (- delivery-time
                                      (:time order)))]
    (concat solution
            (list {:order           order
                   :start-time      start-time
                   :delivery-time   delivery-time
                   :completion-time completion-time
                   :rating          rating }))))


(defmulti delivery-solver
  "Solver interface"
  (fn [name scorer-fn timer-fn orders] name))

;;   "Identity solver, returns orders in order-of-input. O(n)"
(defmethod delivery-solver "identity"
  [_ scorer-fn timer-fn orders]
  (loop [remaining-orders  orders
         solution          '()
         current-time      drone-start-time-s]

    (if (empty? remaining-orders)
      solution
      (let [next-order       (first remaining-orders)
            incr-solution    (add-order-to-solution scorer-fn timer-fn current-time solution next-order)
            completed-time   (:completion-time (last incr-solution))]
        (recur (rest remaining-orders)
               incr-solution
               completed-time)))))



(defn- score-order-seq
  "Score orderseq as a solution"
  [scorer-fn timer-fn order-seq]
  (let [solution   (delivery-solver "identity" scorer-fn timer-fn order-seq)]
    {:solution solution
     :score    (score/extract-solution-nps solution)}))



(defn permute
  "Generate all permutions of collection"
  [colls]
  (if (= 1 (count colls))
    (list colls)
    (for [head colls
          tail (permute (remove #(= % head) colls))]
      (cons head tail))))


;;   "Full permuations solver, finds highest scoring NPS solution,
;;    returns orders in order-of-input. O(n!)"
(defmethod delivery-solver "permuted"
  [_ scorer-fn timer-fn orders]
  (let [perms             (permute orders)
        scored-perms      (map (partial score-order-seq scorer-fn timer-fn) perms)
        highest-score     (apply max (map :score scored-perms))
        best-perms        (filter #(>= (:score %) highest-score) scored-perms)]

    (->> best-perms
         first
         :solution)))


;; TODO: inneficient, use finger-trees/RRBs?
(defn- seq-insert
  [partitioned-seq value position]
  (let [vectorized   (vec partitioned-seq)]
    (concat (take position vectorized)
            (vector value)
            (drop position vectorized))))


(defn- permute-interposition
  "Interposition including head and tail of a collection. (C,AB)=>(CAB,ACB,ABC)"
  [sep seq]
  (for [position    (range 0 (inc (count seq)))]
    (seq-insert seq sep position)))

(defn- ppm
  [msg structure]
  (let [_ (println msg)
        _ (clojure.pprint/pprint structure)]
    nil))


(defn- greedy-permuted-helper
  "Greedy solver partitioning function"
  [scorer-fn timer-fn orders]
  (if (<= (count orders) 1)
    (conj (list) orders)
    (let [last-order          (last orders)
                                        ;_                   (ppm "last order " last-order)
          best-sub-orders     (greedy-permuted-helper scorer-fn timer-fn (drop-last orders))
                                        ;_                   (ppm "best sub orders " best-sub-orders)
          interposed-orders   (apply concat (for [sub-order best-sub-orders]
                                              (permute-interposition last-order sub-order)))
                                        ;_                   (ppm "== Interposed" interposed-orders)
          scored-order-seqs   (map (partial score-order-seq scorer-fn timer-fn) interposed-orders)
                                        ;_                   (ppm "=== Scored order seqs " scored-order-seqs)
          highest-score       (apply max (map :score scored-order-seqs))
                                        ;_                   (println (str "Highest score " highest-score))
          best-order-seqs     (filter #(>= (:score %) highest-score) scored-order-seqs)
                                        ;_                   (ppm "=== Best order seqs" best-order-seqs)
          best-orders         (map (partial map :order)
                                   (map :solution best-order-seqs))
                                        ;_                   (ppm "(RETURNING )Best orders " best-orders)
          ]
      best-orders)))


;;   "O(n^2) Greedy solver. Find near-best solution building
;;    on optimal sub-solutions.  Subject to local maxima holing
;;    e.g.
;;        best-solution (A B C) => best-score(C *permuted* best-solution(A B))
;;                              => best-score(C *permuted* [B A])
;;                              => best-score([C B A] [B C A] [B A C])
;;   "
(defmethod delivery-solver "greedy-permuted"
  [_ scorer-fn timer-fn orders]
  (let [best-order-permutations  (greedy-permuted-helper scorer-fn
                                                         timer-fn
                                                         orders)]
    (->> best-order-permutations
         (first)
         (score-order-seq scorer-fn timer-fn)
         :solution)))
