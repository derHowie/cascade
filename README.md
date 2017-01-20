# cascade

Informs collection items with cascading data from earlier in the collection. 

## Usage

```clojure
[cascade "0.1.0"]
```

In cases where the order of a collection drives a piece of your application, cascade can surface the preceeding data for each item in the collection and allow you to manipulate it.

Here is the simplest use case:

```clojure
(require '[cascade.core :refer [cascade])

(cascade [{:a 1} {:a 2} {:a 3}])
;; => [{:csd () :a 1} {:csd ({:a 1}) :a 2} {:csd ({:a 1} {:a 2}) :a 3}]
```

Each item in the collection receives a lazy seq of the items preceeding it.

This can be done with collections of vectors:

```clojure
(cascade [[1][2][3]])
;; => ([1 ()] [2 ([1])] [3 ([1] [2])])

## License

Copyright © 2017 Christopher Howard

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
