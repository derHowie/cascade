# cascade

Informs collection items with cascading data from earlier in the collection. 

## Usage

```clojure
[cascade "0.1.0"]
```

In cases where the order of a collection drives a piece of your application, cascade can surface the data for each preceeding item in the collection and allow you to manipulate it.

Here is a simple example:

```clojure
(require '[cascade.core :refer [cascade])

(cascade [1 2 3])
;; => ([1 ()] [2 (1)] [3 (1 2)])
```

Each item in the collection receives a lazy seq of the items preceeding it.

This can be done with any data-type:

```clojure
(cascade [[1][2][3]])
;; => ([1 ()] [2 ([1])] [3 ([1] [2])])

(cascade '(#{1} #{2} #{3}))
;; => ([#{1} ()] [#{2} (#{1})] [#{3} (#{1} #{2})])

;; collections of maps receive cascade's output via the :csd key
(cascade [{:a 1} {:a 2} {:a 3}])
;; => ({:csd (), :a 1} {:csd ({:a 1}), :a 2} {:csd ({:a 1} {:a 2}), :a 3})

;; calling cascade on a map of data works, just be aware each key-value pair in the map is treated as a vector
(cascade {:a 1 :b 2 :c 3})
;; => ([:a 1 ()] [:b 2 ([:a 1])] [:c 3 ([:a 1] [:b 2])])
```

Of course, if you are interested in the data preceeding each item, you probably want to call a function on that data. Cascade allows for this:

```clojure
(cascade [{:a 1} {:a 2} {:a 3}] :a)
;; => ({:csd (), :a 1} {:csd (1), :a 2} {:csd (1 2), :a 3})
```

Cascade allows you to provide a predicate function for `reduce` if you want to reduce the seq returned by cascade.

```clojure
(defn some-predicate-fn
  [a b]
  (str a b))

(cascade ["a" "b" "c" "d"] some-predicate-fn :reduce? true)
;; => (["a" nil] ["b" "a"] ["c" "ab"] ["d" "abc"]) 
```

You may also provide a start value value when using `reduce` with cascade.

```clojure
(defn another-predicate-fn
  [a b]
  (* a b)

(cascade '(2 5 10 1) another-predicate-fn :reduce? true :start-value 3) 
;; => ([2 3] [5 6] [10 30] [1 300]) 
```

### Note:

You may have noticed while reading these examples that the reduce function does not include the item it is appended to. I realize in a lot of cases, one may want to include that value, however I believe that type of functionality would be limiting to those who want to reduce the preceeding values then call a different function involving cascade's result and the current item.

### Example:

Let's look at a slightly less contrived example to see why cascade can be useful:

In this scenario we have been contracted to build an application that helps the proprietor of a petting zoo get a better visualization of his day-to-day operations while also balancing the books.

Here is the data we were able to aggregate from the past 5 days of operations.

```clojure
(def cash-on-hand 100)
(def petting-zoo-ops
  [{:day 1
    :patrons 24
    :admission 7.80
    :expenses 54.00}
   {:day 2
    :patrons 37
    :admission 5.20
    :expenses 64.00}
   {:day 3
    :patrons 11
    :admission 9.00
    :expenses 48.00}
   {:day 4
    :patrons 12
    :admission 7.80
    :expenses 10.00}
   {:day 5
    :patrons 26
    :admission 6.80
    :expenses 24.00}])
```

At a glance it's hard to glean a lot of information from this data, but with cascade we're only a few lines from having the information needed to fuel our data visualization components.

```clojure

(defn learn-stuff
  [a b]
  (let [total-patrons (+ (:patrons-to-date a)
                         (:patrons b))
        profit        (- (* (:patrons b)
                            (:admission b))
                         (:expenses b))]
       (-> a
           (assoc :patrons-to-date total-patrons)
           (#(assoc % :ledger-balance (+ (:ledger-balance %)
                                         profit))))))

(filter #(:csd %) (cascade
                    petting-zoo-ops
                    learn-stuff
                    :reduce? true
                    :start-value
                    {:ledger-balance cash-on-hand
                     :patrons-to-date 0}))

;; => ({:ledger-balance 1000,
;; =>   :patrons-to-date 0}
;; =>  {:ledger-balance 1133.20
;; =>   :patrons-to-date 24}
;; =>  {:ledger-balance 1261.60
;; =>   :patrons-to-date 61}
;; =>  {:ledger-balance 1312.60
;; =>   :patrons-to-date 72}
;; =>  {:ledger-balance 1396.20
;; =>   :patrons-to-date 84})
```

Woo! Now we have an collection illustrating the state of affairs at the beginning of each day.

I have found that if you have a lot of information you need to keep a running tally on, like a bank statement for example, this type of output can be very handy. In clojurescript applications with uni-directional data flow, using Reagent for example, calling cascade in the render function can be powerful. Mutating cascade's data source in this case will cause a ripple in your data and everything following the mutated value with be recalculated in a whirlwind of reactive, uni-directional sauciness.


## License

Copyright © 2017 Christopher Howard

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
