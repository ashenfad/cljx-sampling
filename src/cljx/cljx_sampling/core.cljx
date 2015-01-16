;; Adapted from:
;; https://github.com/bigmlcom/sampling/blob/master/src/bigml/sampling/simple.clj
;; ----------------------------------------------
;; Copyright 2013, 2014, 2015 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns cljx-sampling.core
  "Provides in-memory random sampling over collections. The original
   population is kept in memory but the resulting sample set is
   produced as a lazy sequence."
  (:require [cljx-sampling.random :as random]
            [cljx-sampling.util :as util]))

(defn- with-replacement [coll rng]
  (when-not (empty? coll)
    (repeatedly #(coll (random/next-int! rng (count coll))))))

(defn- without-replacement [coll rng]
  (when-not (empty? coll)
    (let [index (random/next-int! rng (count coll))]
      (cons (coll index)
            (-> (subvec (assoc coll index (first coll)) 1)
                (without-replacement rng)
                (lazy-seq))))))

(def ^:private branch-factor 8)

(defn- make-tree [coll weigh]
  (if (> (count coll) branch-factor)
    (let [branch-size (Math/ceil (/ (count coll) branch-factor))
          children (doall (map #(make-tree % weigh)
                               (partition-all branch-size coll)))]
      {:total (reduce + (map :total children))
       :children children})
    {:total (reduce + (map weigh coll))
     :items coll}))

(declare pop-item)

(defn- process-parent [roll weigh [prev-total result item] child]
  (let [new-total (+ prev-total (:total child))]
    (if (and (nil? item)
             (> new-total roll prev-total))
      (let [[new-child item] (pop-item weigh child (- roll prev-total))]
        [(+ prev-total (:total new-child))
         (if (zero? (:total new-child))
           result
           (cons new-child result))
         item])
      [new-total (cons child result) item])))

(defn- process-leaf [roll weigh [prev-total result item] candidate-item]
  (let [new-total (+ prev-total (weigh candidate-item))]
    (if (and (nil? item)
             (> new-total roll prev-total))
      [prev-total result candidate-item]
      [new-total (cons candidate-item result) item])))

(defn- pop-item [weigh {:keys [total children items]} roll]
  (if children
    (let [[new-total new-children item]
          (reduce (partial process-parent roll weigh) [0] children)]
      [{:total new-total :children new-children} item])
    (let [[new-total new-items item]
          (reduce (partial process-leaf roll weigh) [0] items)]
      [{:total new-total :items new-items} item])))

(defn- weighted-without-replacement [tree weigh rng]
  (when (pos? (:total tree))
    (let [[new-tree item]
          (pop-item weigh tree (random/next-double! rng (:total tree)))]
      (cons item (lazy-seq (weighted-without-replacement new-tree weigh rng))))))

(defn- weighted-with-replacement [coll weigh rng]
  (let [sm (->> (reductions (fn [[tw pv] v]
                              (let [w (weigh v)]
                                (if (zero? w)
                                  [tw pv]
                                  [(+ tw w) v])))
                            [0]
                            coll)
                (next)
                (into (sorted-map)))
        total (first (last sm))]
    (repeatedly
     #(second (first (subseq sm > (random/next-double! rng total)))))))

(defn sample
  "Returns a lazy sequence of samples from the collection.  The
   collection is kept in memory.
   Options:
    :replace - True to sample with replacement, defaults to false.
    :seed - A seed for the random number generator, defaults to nil.
    :weigh - A function that returns a non-negative weight for an
             item.  When nil, no sampling weights are applied.
             Defaults to nil."
  [coll & opts]
  (if-not (vector? coll)
    (apply sample (vec coll) opts)
    (let [{:keys [replace weigh seed]} opts
          weigh (util/validated-weigh weigh)
          rng (random/create seed)]
      (cond (and replace weigh)
            (weighted-with-replacement coll weigh rng)
            weigh
            (weighted-without-replacement (make-tree coll weigh) weigh rng)
            replace
            (with-replacement coll rng)
            :else
            (without-replacement coll rng)))))
