(ns clchess.chess
  (:require [taoensso.timbre :as log]
            #?(:clj [clojure.spec :as s]
               :cljs [cljs.spec :as s])
            #?(:clj [clojure.spec.test :as stest :include-macros true]
               :cljs [cljs.spec.test :as stest :include-macros true])
            [clchess.specs.chess :as schess]
            [clchess.specs.board :as sboard]
            [clchess.data.board :as dboard]
            [clj-chess.board :as board]
            [clj-chess.game :as game]
            [clj-chess.pgn :refer [parse-pgn]]))

(s/def ::square int?)
(s/def ::piece int?)
(s/def ::from ::square)
(s/def ::to ::square)
(s/def ::castle boolean?)
(s/def ::promotion (s/or :false false? :piece ::piece))
(s/def ::ep boolean?)
(s/def ::move (s/keys :req-un [::from
                               ::to
                               ::castle
                               ::promotion
                               ::ep]))

(defn- create-chess
  ([]
   (game/new-game))
  ([fen]
   {:pre [(s/valid? ::schess/fen fen)]}
   (game/new-game :start-fen fen)))

(defn- color [chess]
  {:post [(s/valid? ::schess/color %)]}
  (case (game/side-to-move chess)
    :white "w"
    :black "b"))

#?(:cljs
   (defn dest-squares [clj-chess]
     {:post [(s/valid? ::sboard/dests %)]}
     (into {}
           (map (fn [[k v]] (vector (board/square-to-string k)
                                    (into #{} (map #(-> %
                                                        (get :to)
                                                        (board/square-to-string))
                                                   v))))
                (group-by :from (-> clj-chess
                                    game/board
                                    board/moves
                                    (js->clj :keywordize-keys true))))))
   :clj
   (defn dest-squares [clj-chess]
     {:post [(s/valid? ::sboard/dests %)]}
     (into {}
           (let [moves-grouped (-> clj-chess
                                   game/board
                                   board/board-to-map
                                   (get :moves)
                                   (as-> moves (group-by :from moves)))]
             (map (fn [[from dests]] (vector (board/square-to-string from)
                                             (into #{} (map #(-> %
                                                                 (get :to)
                                                                 board/square-to-string)
                                                            dests))))
                  moves-grouped)))))

(defn compute-state [{:keys [::schess/initial-fen
                             ::schess/moves
                             ::schess/current-ply] :as state}]
  (let [last-move (when (> current-ply 0) (nth moves (- current-ply 1)))
        fen (if (or (empty? moves)
                    (= current-ply 0))
              initial-fen
              (::schess/fen last-move))
        chess (game/new-game :start-fen fen)]
    {::schess/fen fen
     ::schess/color (color chess)
     ::schess/last-move last-move
     ::sboard/dests (dest-squares chess)}))

(s/fdef compute-state
        :args (s/cat :state ::schess/game)
        :ret (s/keys :req [::schess/fen
                           ::schess/color
                           ::sboard/dests]
                     :opt [::schess/last-move]))

(stest/instrument `compute-state)

(defn adapt-piece [piece]
  (case piece
    "p" board/pawn
    "r" board/rook
    "k" board/king
    "b" board/bishop
    "q" board/queen
    "n" board/knight))

(defn adapt-piece-kw [kw]
  (let [[_ color piece] (str kw)]
    [(str color) (str piece)]))

#?(:clj
   (defn move [chess from to promotion]
     (let [board (game/board chess)
           side (board/side-to-move board)
           from (board/square-from-string from)
           to (board/square-from-string to)
           possible-moves (board/moves-from board from)]
       (first (filter #(and (= to (get % :to))
                            (or (not promotion)
                                (and promotion
                                     (let [piece-kw (get % :promote-to)
                                           [_ piece] (adapt-piece-kw piece-kw)]
                                       (= piece promotion)))))
                      (map #(board/move-to-map board %)
                           possible-moves)))))
   :cljs
   (defn move [chess from to promotion]
     (let [from (board/square-from-string from)
           to (board/square-from-string to)
           board (game/board chess)
           move (first (filter
                        #(and (= (aget % "to") to)
                              (or (not promotion)
                                  (and promotion
                                       (= (aget % "promotion") (adapt-piece promotion)))))
                        (-> board
                            (board/moves-from from))))]
       move)))

;;(move (create-chess) "d2" "d4" nil)
;;(move (create-chess "rnbq1rk1/pP2bppp/4pn2/8/3P4/5N2/PP2PPPP/RNBQKB1R w KQ - 1 7") "b7" "a8" "q")

#?(:clj
   (defn make-move [{:keys [::schess/initial-fen
                            ::schess/moves
                            ::schess/current-ply] :as state} from to & {:keys [::schess/promotion] :as options}]
     {:post [(s/valid? ::schess/game %)]}
     (let [{fen ::schess/fen} (compute-state state)
           chess (create-chess fen)
           board (game/board chess)
           move (move chess from to promotion)
           fen (-> board
                   (board/do-map-move move)
                   board/to-fen)
           move-info {::schess/from (board/square-to-string (get move :from))
                      ::schess/to (board/square-to-string (get move :to))
                      ::schess/fen fen}]
       (-> state
           (update ::schess/moves #(conj % move-info))
           (update ::schess/current-ply inc))))
   :cljs
   (defn make-move [{:keys [::schess/initial-fen
                             ::schess/moves
                             ::schess/current-ply] :as state} from to & {:keys [::schess/promotion] :as options}]
      {:post [(s/valid? ::schess/game %)]}
      (let [{fen ::schess/fen} (compute-state state)
            chess (create-chess fen)
            board (game/board chess)
            move (move chess from to promotion)
            fen (-> board
                    (board/do-move move)
                    board/to-fen)
            move-info {::schess/from (board/square-to-string (aget move "from"))
                       ::schess/to (board/square-to-string (aget move "to"))
                       ::schess/fen fen}]
        (-> state
            (update ::schess/moves #(conj % move-info))
            (update ::schess/current-ply inc)))))

(s/fdef make-move
        :args (s/cat :state ::schess/game
                     :from ::sboard/square
                     :to ::sboard/square
                     :options (s/keys* :opt [::schess/promotion]))
        :ret ::schess/game)

(stest/instrument `make-move)


(s/fdef load-pgn
        :args (s/cat :state ::schess/game
                     :pgn ::schess/pgn)
        :ret ::schess/game)

(stest/instrument `load-pgn)

(defn load-pgn [{:keys [::schess/initial-fen
                        ::schess/moves
                        ::schess/current-ply] :as state} pgn]
  (let [chess (game/from-pgn pgn)
        boards-moves (game/boards-with-moves chess)]
    {::schess/initial-fen (or (and boards-moves
                                   (let [[board _] (first boards-moves)]
                                     (board/to-fen board)))
                              board/start-fen)
     ::schess/current-ply 0
     ::schess/moves
     (for [[board move] boards-moves
           :let [{:keys [to from]} (board/move-to-map board move)]]
       {::schess/fen (board/to-fen board)
        ::schess/from (board/square-to-string from)
        ::schess/to (board/square-to-string to)})}))

