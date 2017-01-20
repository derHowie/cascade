(ns cascade.core)

(defn flow
  "returns the preceeding items in the collection"
  [idx c]
  (take idx c))

(defn trickle
  "reduces flow using a predicate function and optional starting value"
  [idx c f & [start-value]]
  (cond
    start-value       (reduce f start-value (flow idx c))
    (not (zero? idx)) (reduce f (flow idx c))
    :else             nil))

(defn cascade
  ([c] (cascade c #(identity %)))
  ([c f & {:keys [reduce? start-value]}]
   (let [percolate (if reduce?
                     (fn [idx] (trickle idx c f start-value))
                     (fn [idx] (map f (flow idx c))))]
     (map-indexed
       (fn [idx itm]
         (cond
           (map? itm)    (merge {:csd (percolate idx)} itm) 
           (vector? itm) (conj itm (percolate idx)) 
           :else         [itm (percolate idx)]))
       c))))
