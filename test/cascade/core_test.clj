(ns cascade.core-test
  (:require [clojure.test :refer :all]
            [cascade.core :refer :all]))

(def test-map-vector [{:val 2} {:val 6} {:val 1} {:val 3}])
(def test-vec-vector [[1] [2] [3]])
(defn reduction-predicate [a b] (assoc a :val (+ (:val a) (:val b))))
(defn reduction-vec-predicate [a b] (conj a (first b)))

(deftest first-arity
  (testing "returns preceeding maps in the collection in each 'before' key"
    (testing "maps: "
      (let [result (cascade test-map-vector)]
        (is (= (:csd (nth result 0)) ()))
        (is (= (:csd (nth result 1)) '({:val 2})))
        (is (= (:csd (nth result 3)) '({:val 2} {:val 6} {:val 1})))))
    (testing "vectors: "
      (let [result (cascade test-vec-vector)]
        (is (= (second (nth result 1)) '([1])))
        (is (= (second (nth result 2)) '([1] [2])))))))

(deftest predicate-fn
  (testing "predicate function is applied to preceeding items in collection then returned in each 'before' key"
    (testing "maps: "
      (let [result (cascade test-map-vector :val)]
        (is (= (:csd (nth result 1)) '(2)))
        (is (= (:csd (nth result 2)) '(2 6)))
        (is (= (:csd (nth result 3)) '(2 6 1)))))
    (testing "vectors: "
      (let [result (cascade test-vec-vector first)]
        (is (= (second (nth result 1)) '(1)))
        (is (= (second (nth result 2)) '(1 2)))))))

(deftest reduce-option
  (testing "preceeding items in collection are reduced via predicate function"
    (testing "maps: "
      (let [result (cascade test-map-vector reduction-predicate :reduce? true)]
        (is (= (:csd (nth result 1)) {:val 2}))
        (is (= (:csd (nth result 2)) {:val 8}))
        (is (= (:csd (nth result 3)) {:val 9}))))
    (testing "vectors: "
      (let [result (cascade test-vec-vector reduction-vec-predicate :reduce? true)]
        (is (= (second (nth result 1)) [1]))
        (is (= (second (nth result 2)) [1 2]))))))

(deftest predicate-fn-with-val
  (testing "predicate function is applied to preceeding items in collection then returned in each 'before' key"
    (testing "maps: " (let [result (cascade test-map-vector reduction-predicate :reduce? true :reduce-val {:val 1})]
      (is (= (:csd (nth result 1)) {:val 3}))
      (is (= (:csd (nth result 2)) {:val 9}))
      (is (= (:csd (nth result 3)) {:val 10}))))
    (testing "vectors: "
      (let [result (cascade test-vec-vector reduction-vec-predicate :reduce? true :reduce-val [9])]
        (is (= (second (nth result 1)) [9 1]))
        (is (= (second (nth result 2)) [9 1 2]))))))

(deftest data-type
  (testing "an exception is thrown if the data type of the collection items is not a map or a vector"
    (is (thrown? Exception (cascade [1 2 3])))))
