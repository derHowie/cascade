(ns cascade.core-test
  (:require [clojure.test :refer :all]
            [cascade.core :refer :all]))

(def test-vector [{:val 2} {:val 6} {:val 1} {:val 3}])
(defn reduction-predicate [a b] (assoc a :val (+ (:val a) (:val b))))


(deftest first-arity
  (testing "returns preceeding maps in the vector in each 'before' key"
    (let [result (cascade test-vector)]
      (is (= (:before (nth result 0)) ()))
      (is (= (:before (nth result 1)) '({:val 2})))
      (is (= (:before (nth result 3)) '({:val 2} {:val 6} {:val 1}))))))

(deftest predicate-fn
  (testing "predicate function is applied to preceeding items in collection then returned in each 'before' key"
    (let [result (cascade test-vector :val)]
      (is (= (:before (nth result 1)) '(2)))
      (is (= (:before (nth result 2)) '(2 6)))
      (is (= (:before (nth result 3)) '(2 6 1))))))

(deftest reduce-option
  (testing "preceeding items in collection are reduced via predicate function"
    (let [result (cascade test-vector reduction-predicate :reduce? true)]
      (is (= result 100)))))

(deftest predicate-fn-with-val
  (testing "predicate function is applied to preceeding items in collection then returned in each 'before' key"
    (let [result (cascade test-vector reduction-predicate :reduce? true :reduce-val {:val 1})]
      (is (= (:before (nth result 1)) {:val 3}))
      (is (= (:before (nth result 2)) {:val 9}))
      (is (= (:before (nth result 3)) {:val 10})))))
