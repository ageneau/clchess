(ns clchess.core-test
  (:require-macros [cljs.test :refer (is deftest testing)])
  (:require [cljs.test]))

(deftest example-passing-test
  (is (= 1 1)))

#_(deftest example-not-passing-test
  (is (= 1 30)))
