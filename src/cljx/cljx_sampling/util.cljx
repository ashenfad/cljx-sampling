;; Adapted from:
;; https://github.com/bigmlcom/sampling/blob/master/src/bigml/sampling/util.clj
;; ----------------------------------------------
;; Copyright 2013, 2014, 2015 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns cljx-sampling.util)

(defn- throw-str [txt]
  (throw #+clj (Exception. txt) #+cljs (js/Error. txt)))

(defn validated-weigh
  "Returns a 'weigh' function whose output is validated."
  [weigh]
  (when weigh
    #(let [weight (weigh %)]
       (cond (nil? weight)
             (throw-str (str "Weight is nil." "\n  Item:" %))
             (not (number? weight))
             (throw-str (str "Weight is not a number."
                              "\n  Item: " % "\n  Weight: " weight))
             (neg? weight)
             (throw-str (str "Weight is negative."
                              "\n  Item: " % "\n  Weight: " weight))
             :else weight))))
