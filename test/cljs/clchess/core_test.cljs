(ns clchess.core-test
  (:require-macros [cljs.test :refer (is deftest testing)])
  (:require [cljs.test]))

(deftest example-passing-test
  (is (= 1 1)))

#_(deftest example-not-passing-test
  (is (= 1 30)))

(def tmp3 #:clchess.specs.chess {:initial-fen
                                 "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                                 :moves
                                 [#:clchess.specs.chess{:color "w",
                                                        :from "d2",
                                                        :to "d4",
                                                        :flags "b",
                                                        :piece "p",
                                                        :san "d4",
                                                        :fen
                                                        "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 1"}
                                  #:clchess.specs.chess{:color "b",
                                                        :from "d7",
                                                        :to "d5",
                                                        :flags "b",
                                                        :piece "p",
                                                        :san "d5",
                                                        :fen
                                                        "rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq d6 0 2"}
                                  #:clchess.specs.chess{:color "w",
                                                        :from "c2",
                                                        :to "c4",
                                                        :flags "b",
                                                        :piece "p",
                                                        :san "c4",
                                                        :fen
                                                        "rnbqkbnr/ppp1pppp/8/3p4/2PP4/8/PP2PPPP/RNBQKBNR b KQkq c3 0 2"}
                                  #:clchess.specs.chess{:color "b",
                                                        :from "c7",
                                                        :to "c6",
                                                        :flags "n",
                                                        :piece "p",
                                                        :san "c6",
                                                        :fen
                                                        "rnbqkbnr/pp2pppp/2p5/3p4/2PP4/8/PP2PPPP/RNBQKBNR w KQkq - 0 3"}
                                  {:captured "p",
                                   :clchess.specs.chess/color "w",
                                   :clchess.specs.chess/from "c4",
                                   :clchess.specs.chess/to "d5",
                                   :clchess.specs.chess/flags "c",
                                   :clchess.specs.chess/piece "p",
                                   :clchess.specs.chess/san "cxd5",
                                   :clchess.specs.chess/fen
                                   "rnbqkbnr/pp2pppp/2p5/3P4/3P4/8/PP2PPPP/RNBQKBNR b KQkq - 0 3"}
                                  #:clchess.specs.chess{:color "b",
                                                        :from "g8",
                                                        :to "f6",
                                                        :flags "n",
                                                        :piece "n",
                                                        :san "Nf6",
                                                        :fen
                                                        "rnbqkb1r/pp2pppp/2p2n2/3P4/3P4/8/PP2PPPP/RNBQKBNR w KQkq - 1 4"}
                                  {:captured "p",
                                   :clchess.specs.chess/color "w",
                                   :clchess.specs.chess/from "d5",
                                   :clchess.specs.chess/to "c6",
                                   :clchess.specs.chess/flags "c",
                                   :clchess.specs.chess/piece "p",
                                   :clchess.specs.chess/san "dxc6",
                                   :clchess.specs.chess/fen
                                   "rnbqkb1r/pp2pppp/2P2n2/8/3P4/8/PP2PPPP/RNBQKBNR b KQkq - 0 4"}
                                  #:clchess.specs.chess{:color "b",
                                                        :from "f6",
                                                        :to "e4",
                                                        :flags "n",
                                                        :piece "n",
                                                        :san "Ne4",
                                                        :fen
                                                        "rnbqkb1r/pp2pppp/2P5/8/3Pn3/8/PP2PPPP/RNBQKBNR w KQkq - 1 5"}
                                  {:captured "p",
                                   :clchess.specs.chess/color "w",
                                   :clchess.specs.chess/from "c6",
                                   :clchess.specs.chess/to "b7",
                                   :clchess.specs.chess/flags "c",
                                   :clchess.specs.chess/piece "p",
                                   :clchess.specs.chess/san "cxb7",
                                   :clchess.specs.chess/fen
                                   "rnbqkb1r/pP2pppp/8/8/3Pn3/8/PP2PPPP/RNBQKBNR b KQkq - 0 5"}
                                  #:clchess.specs.chess{:color "b",
                                                        :from "e4",
                                                        :to "c3",
                                                        :flags "n",
                                                        :piece "n",
                                                        :san "Nc3",
                                                        :fen
                                                        "rnbqkb1r/pP2pppp/8/8/3P4/2n5/PP2PPPP/RNBQKBNR w KQkq - 1 6"}],
                                 :current-ply 10})

(make-move tmp3 "b7" "a8" {:promoting false, :player "black"})
