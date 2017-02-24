(ns clchess.core-test
  (:require-macros [cljs.test :refer (is deftest testing)])
  (:require [cljs.test]
            [clchess.ctrl :as ctrl]))

(deftest bishop-diag
  (is (= (ctrl/moves (ctrl/create-chess "rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq d6 0 2") "c1")
         {"c1" '("d2" "e3" "f4" "g5" "h6")})))

