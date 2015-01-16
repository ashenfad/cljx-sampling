(ns cljx-sampling.core-test
  #+cljs (:require-macros [cemerick.cljs.test :refer (deftest is are)])
  (:require [cljx-sampling.random :as random]
            [cljx-sampling.core :refer [sample]]
            #+cljs [cemerick.cljs.test :as t]
            #+clj [clojure.test :refer (deftest is are)]))

(defn- int-seq [pop max-range seed]
  (let [rng (random/create seed)]
    (repeatedly pop #(random/next-int! rng max-range))))

(deftest rand-test
  (is (= (frequencies (int-seq 10000 5 "foo"))
         {0 1976, 2 2006, 4 2040, 3 2036, 1 1942}))
  (is (= (frequencies (int-seq 10000 5 12345))
         {2 2064, 0 1933, 4 2038, 3 2002, 1 1963})))

(deftest sample-test
  (is (= (sample (range 10) :seed "foo")
         '(0 6 2 9 3 8 5 4 1 7)))
  (is (= (sample (range 10) :seed "foo" :weigh #(Math/pow 2 %))
         '(6 9 4 5 8 7 3 0 1 2)))
  (is (= (->> (sample (range 6) :seed "foo" :weigh identity :replace true)
              (take 10000)
              (frequencies)
              (sort-by second >))
         '([5 3405] [4 2677] [3 1942] [2 1336] [1 640]))))

(deftest zero-weight
  (is (= {:heads 100}
         (->> (sample [:heads :tails]
                      :replace true
                      :weigh {:heads 1 :tails 0})
              (take 100)
              (frequencies)))))
