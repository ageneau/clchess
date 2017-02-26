(ns clchess.core-test
  (:require-macros [cljs.test :refer (is deftest testing)])
  (:require [cljs.test]
            [clchess.ctrl :as ctrl]
            [clchess.specs.board :as sboard]
            [clchess.specs.chess :as schess]))

(deftest bishop-diag
  (is (= (ctrl/moves (ctrl/create-chess "rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq d6 0 2") "c1")
         {"c1" '("d2" "e3" "f4" "g5" "h6")})))


(deftest make-move-1
  (let [initial
        {::schess/initial-fen
         "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
         ::schess/moves
         []
         ::schess/current-ply 0}
        expected
        {::schess/initial-fen
         "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
         ::schess/moves
         [{::schess/color "w"
           ::schess/from "d2"
           ::schess/to "d4"
           ::schess/flags "b"
           ::schess/piece "p"
           ::schess/san "d4"
           ::schess/fen
           "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 1"}]
         ::schess/current-ply 1}]
    (is (= expected (ctrl/make-move initial "d2" "d4")))))

(deftest make-move-promotion
  (let [initial
        {::schess/initial-fen
         "rnbq1rk1/pP2bppp/4pn2/8/3P4/5N2/PP2PPPP/RNBQKB1R w KQ - 1 7"
         ::schess/moves
         []
         ::schess/current-ply 0}
        expected
        {::schess/initial-fen
         "rnbq1rk1/pP2bppp/4pn2/8/3P4/5N2/PP2PPPP/RNBQKB1R w KQ - 1 7"
         ::schess/moves
         [{::schess/from "b7"
           ::schess/captured "r"
           ::schess/flags "cp"
           ::schess/fen
           "Qnbq1rk1/p3bppp/4pn2/8/3P4/5N2/PP2PPPP/RNBQKB1R b KQ - 0 7"
           ::schess/promotion "q"
           ::schess/to "a8"
           ::schess/color "w"
           ::schess/san "bxa8=Q"
           ::schess/piece "p"}]
         ::schess/current-ply 1}]
    (is (= expected (ctrl/make-move initial "b7" "a8" ::schess/promotion "q")))))
