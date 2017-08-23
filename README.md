# cljx-sampling

This Clojure/ClojureScript library provides consistent sampling and
random number generation regardless of the environment. That means
using the same intial seed we can produce the exact same results
(sampled items or random numbers) whether in the JVM or in the
browser.

## Installation

`cljx-sampling` is available as a Maven artifact from
[Clojars](https://clojars.org/cljx-sampling).

For [Leiningen](https://github.com/technomancy/leiningen):

[![Clojars Project](http://clojars.org/cljx-sampling/latest-version.svg)](http://clojars.org/cljx-sampling)

## Overview

This library has two useful namespaces:
  - [`cljx-sampling.random`](#random-number-generation) : Offers seedable random number generation.
  - [`cljx.sampling.core`](#sampling) : In-memory sampling over collections (borrows from
[bigml/sampling](https://github.com/bigmlcom/sampling#simple-sampling)).

As we review each, feel free to follow along in the REPL:
```clojure
user> (ns demo
        (:require [cljx-sampling.random :as random]
                  [cljx-sampling.core :refer [sample]]))
```

## Random number generation

`cljx-sampling.random` provides seedable random number generation
using a [32-bit Xorshift](http://www.jstatsoft.org/v08/i14/paper).
While this random number generator is surprisingly strong for its
simplicity, it is not cryptographically secure.

To use, simply `create` a random number generator and then call
`next-boolean!`, `next-int!`, `next-double!`, or `next-gaussian!` as
needed.

```clojure
demo> (def rng (random/create))
demo> (random/next-boolean! rng)
true
demo> (random/next-int! rng)
3321045053
demo> (random/next-int! rng 8)
4
demo> (random/next-double! rng)
0.5436311555095017
demo> (random/next-double! rng 10)
8.794576390646398
demo> (random/next-gaussian! rng)
0.09573863197719758
```

When we `create` a generator, we can provide a seed to make the
numbers deterministic.

```clojure
demo> (def rng1 (random/create "foobar"))
demo> (def rng2 (random/create "foobar"))
demo> (repeatedly 10 #(random/next-int! rng1 100))
(51 14 22 53 21 32 9 97 63 65)
demo> (repeatedly 10 #(random/next-int! rng2 100))
(51 14 22 53 21 32 9 97 63 65)
```

## Sampling

`cljx-sampling.core` provides in-memory random sampling over
collections. While the original population is kept in memory, the
resulting sample is a lazy sequence.

By default, sampling is done [without replacement](http://www.ma.utexas.edu/users/parker/sampling/repl.htm). This
is equivalent to a lazy [Fisher-Yates shuffle](http://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle).

```clojure
demo> (sample (range 5))
(2 3 1 0 4)
```

Setting `:replace` as true will sample with replacement. Since there
is no limit to the number of items that may be sampled with
replacement from a population, the result will be an infinite length
list. So make sure to `take` however many samples are needed.

```clojure
demo> (take 10 (sample (range 5) :replace true))
(2 3 3 2 4 1 1 1 3 0)
```

Each call to `sample` will return a new sample order.

```clojure
demo> (sample (range 5))
(0 2 3 1 4)
demo> (sample (range 5))
(3 1 4 2 0)
```

Setting the `:seed` parameter allows the sample order to be
deterministic.

```clojure
demo> (sample (range 5) :seed 7)
(1 4 2 0 3)
demo> (sample (range 5) :seed 7)
(1 4 2 0 3)
demo> (sample (range 5) :seed "foobar")
(2 1 0 4 3)
demo> (sample (range 5) :seed "foobar")
(2 1 0 4 3)
```

### Weighted Sampling

A sample may be weighted using the `:weigh` parameter. If the
parameter is supplied with a function that takes an item and produces
a non-negative weight, then the resulting sample will be weighted
accordingly.

```clojure
demo> (take 5 (sample [:heads :tails]
                      :weigh {:heads 0.5 :tails 0.5}
                      :replace true))
(:tails :heads :heads :heads :tails)
```

The weights need not sum to 1.

```clojure
demo> (->> (sample [:heads :tails]
                   :weigh {:heads 2 :tails 1}
                   :replace true)
           (take 100)
           (frequencies))
{:heads 66, :tails 34}
```

## License

Copyright (C) 2014-2017 - Adam Ashenfelter

Distributed under the Apache License, Version 2.0.
