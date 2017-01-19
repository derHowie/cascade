(ns cascade.core)

(defn flow
  "returns the preceeding items in the collection"
  [idx c]
  (take idx c))

(defn trickle
  "reduces flow using a predicate function and optional starting value"
  [idx c f & [reduce-val]]
  (cond
    reduce-val        (reduce f reduce-val (flow idx c))
    (not (zero? idx)) (reduce f (flow idx c))
    :else             nil))

(defn cascade
  ([c] (cascade-2 c #(identity %)))
  ([c f & {:keys [reduce? reduce-val]}]
   (let [percolate (if reduce?
                     (fn [idx] (trickle idx c f reduce-val))
                     (fn [idx] (map f (flow idx c))))]
     (map-indexed
       (fn [idx itm]
         (cond
           (map? itm)    (merge {:csd (percolate idx)} itm) 
           (vector? itm) (conj itm (percolate idx)) 
           :else         (throw (Exception. "Cascade: collection item must be a map or a vector."))))
       c))))


;; confluence
;; take-nth
;; take-while?
