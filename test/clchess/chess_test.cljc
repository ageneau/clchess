(ns clchess.chess-test
  (:require #?(:clj [clojure.test :refer (is deftest testing)]
               :cljs [cljs.test :refer (is deftest testing)])
            #?(:clj [clojure.spec :as s]
               :cljs [cljs.spec :as s])
            #?(:clj [clojure.spec.test :as stest :include-macros true]
               :cljs [cljs.spec.test :as stest :include-macros true])
            [clchess.chess :as chess]
            [clchess.specs.board :as sboard]
            [clchess.specs.chess :as schess]
            [clj-chess.board :refer [to-fen start-fen]]
            [clj-chess.game :as game]
            [clj-chess.pgn :refer [parse-pgn]]))

(deftest en-passant-in-fen
  (let [g0 (-> (game/new-game)
               (game/add-san-move "d4"))]
    (is (= (-> g0 game/board to-fen)
           "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 1"))))

(deftest compute-state-1
  (is (= (chess/compute-state {::schess/initial-fen
                               "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                               ::schess/moves
                               []
                               ::schess/current-ply 0})
         {:clchess.specs.chess/fen
          "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
          :clchess.specs.chess/color "w",
          :clchess.specs.chess/last-move nil,
          :clchess.specs.board/dests
          {"f2" #{"f3" "f4"},
           "e2" #{"e3" "e4"},
           "g2" #{"g3" "g4"},
           "h2" #{"h3" "h4"},
           "d2" #{"d3" "d4"},
           "c2" #{"c3" "c4"},
           "b2" #{"b3" "b4"},
           "g1" #{"f3" "h3"},
           "a2" #{"a3" "a4"},
           "b1" #{"a3" "c3"}}})))

(deftest compute-state-2
  (is (= (chess/compute-state {::schess/initial-fen
                               "rnbq1rk1/pP2bppp/4pn2/8/3P4/5N2/PP2PPPP/RNBQKB1R w KQ - 1 7"
                               ::schess/moves
                               []
                               ::schess/current-ply 0})
         {:clchess.specs.chess/fen
          "rnbq1rk1/pP2bppp/4pn2/8/3P4/5N2/PP2PPPP/RNBQKB1R w KQ - 1 7",
          :clchess.specs.chess/color "w",
          :clchess.specs.chess/last-move nil,
          :clchess.specs.board/dests
          {"d4" #{"d5"},
           "e2" #{"e3" "e4"},
           "f3" #{"e5" "g5" "h4" "g1" "d2"},
           "b7" #{"a8" "c8"},
           "d1" #{"c2" "b3" "a4" "d2" "d3"},
           "g2" #{"g3" "g4"},
           "h2" #{"h3" "h4"},
           "e1" #{"d2"},
           "b2" #{"b3" "b4"},
           "h1" #{"g1"},
           "a2" #{"a3" "a4"},
           "c1" #{"d2" "e3" "f4" "g5" "h6"},
           "b1" #{"a3" "c3" "d2"}}})))

(def initial-game {::schess/initial-fen
                   "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                   ::schess/moves
                   []
                   ::schess/current-ply 0})

(deftest make-move-1
  (let [expected
        {::schess/initial-fen
         "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
         ::schess/moves
         [{;;::schess/color "w"
           ::schess/from "d2"
           ::schess/to "d4"
           ;;::schess/flags "b"
           ;;::schess/piece "p"
           ;;::schess/san "d4"
           ::schess/fen
           "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 1"}]
         ::schess/current-ply 1}]
    (is (= expected (chess/make-move initial-game "d2" "d4")))))

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
           ;;::schess/captured "r"
           ;;::schess/flags "cp"
           ::schess/fen
           "Qnbq1rk1/p3bppp/4pn2/8/3P4/5N2/PP2PPPP/RNBQKB1R b KQ - 0 7"
           ;;::schess/promotion "q"
           ::schess/to "a8"
           ;;::schess/color "w"
           ;;::schess/san "bxa8=Q"
           ;; ::schess/piece "p"
           }]
         ::schess/current-ply 1}]
    (is (= expected (chess/make-move initial "b7" "a8" ::schess/promotion "q")))))

(def pgn-string-0
  "[Event \"F/S Return Match\"]
[Site \"Belgrade, Serbia JUG\"]
[Date \"1992.11.04\"]
[Round \"29\"]
[White \"Fischer, Robert J.\"]
[Black \"Spassky, Boris V.\"]
[Result \"1/2-1/2\"]

1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3
O-O 9. h3 Nb8 10. d4 Nbd7 11. c4 c6 12. cxb5 axb5 13. Nc3 Bb7 14. Bg5 b4 15.
Nb1 h6 16. Bh4 c5 17. dxe5 Nxe4 18. Bxe7 Qxe7 19. exd6 Qf6 20. Nbd2 Nxd6 21.
Nc4 Nxc4 22. Bxc4 Nb6 23. Ne5 Rae8 24. Bxf7+ Rxf7 25. Nxf7 Rxe1+ 26. Qxe1 Kxf7
27. Qe3 Qg5 28. Qxg5 hxg5 29. b3 Ke6 30. a3 Kd6 31. axb4 cxb4 32. Ra5 Nd5 33.
f3 Bc8 34. Kf2 Bf5 35. Ra7 g6 36. Ra6+ Kc5 37. Ke1 Nf4 38. g3 Nxh3 39. Kd2 Kb5
40. Rd6 Kc5 41. Ra6 Nf2 42. g4 Bd3 43. Re6 1/2-1/2")

(deftest load-pgn
  (let [game (chess/load-pgn start-fen pgn-string-0)
        last-pos (-> game
                     (get ::schess/moves)
                     (last)
                     (get ::schess/fen))]
    (is (= last-pos "8/8/R5p1/2k3p1/1p4P1/1P1b1P2/3K1n2/8 w - - 1 43"))))
