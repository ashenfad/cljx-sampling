(ns cljx-sampling.random
  "Provides a seedable random number generator using a 32 bit Xorshift.
   http://www.jstatsoft.org/v08/i14/paper")

(def ^:private unsigned-int-range (Math/pow 2 32))
(def ^:private signed-int-range (Math/pow 2 31))

(declare next-double!)

(defn- shift-left [v pos]
  (-> (bit-shift-left v pos) #+clj (bit-and 0xffffffff)))

(defn- xor-shift [v]
  (as-> v v
    (bit-xor v (shift-left v 13))
    (bit-xor v (unsigned-bit-shift-right v 17))
    (bit-xor v (shift-left v 5))))

(defn- next! [rng bits]
  (swap! rng xor-shift)
  (unsigned-bit-shift-right @rng (- 32 bits)))

(defn create
  "Creates a random number generator with an optional seed.  The 'str'
   value of the given seed is hashed and used as the final seed."
  [& [seed]]
  (atom (if seed
          ;; CLJ and CLJS will hash strings to the same value
          (hash (str seed))
          (rand-int signed-int-range))))

(defn next-boolean!
  "Generates a boolean given a random number generator."
  [rng]
  (pos? (next! rng 1)))

(defn next-int!
  "Generates an integer from [0, 2^32) given a random number
   generator, or [0, max-range) with an optional max range.  Note that
   on the JVM this fn will return a Long."
  ([rng]
   (next! rng 32))
  ([rng max-range]
   (#+clj long #+cljs int (* max-range (next-double! rng)))))

(defn next-double!
  "Generates a double from [0,1) given a random number generator, or
   [0, max-range) with an optional max-range."
  ([rng]
   (/ (next-int! rng) unsigned-int-range))
  ([rng max-range]
   (* max-range (next-double! rng))))

(defn next-gaussian!
  "Generates a double from a normal distribution given a random number
   generator and an optional mean (default 0) and standard
   deviation (default 1)."
  [rng & [mean std-dev]]
  ;; Uses the Marsaglia polar method, but returns only one sample.
  ;; http://en.wikipedia.org/wiki/Marsaglia_polar_method
  (let [x (dec (next-double! rng 2))
        y (dec (next-double! rng 2))
        s (+ (* x x) (* y y))]
    (if (or (>= s 1) (zero? s))
      (next-gaussian! rng mean std-dev)
      (+ (or mean 0)
         (* x
            (or std-dev 1)
            (Math/sqrt (* -2 (/ (Math/log s) s))))))))
