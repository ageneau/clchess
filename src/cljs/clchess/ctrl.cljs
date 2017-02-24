(ns clchess.ctrl
  (:require [clchess.utils :as utils]
            [taoensso.timbre :as log]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [cljs.spec :as s]
            [cljs.spec.test :as stest :include-macros true]
            [clojure.test.check :as tc]
            [clchess.specs.chess :as schess]
            [clchess.specs.board :as sboard]
            [cljsjs.chess.js]))

(defn- create-chess
  ([]
   (js/Chess.))
  ([fen]
   {:pre [(s/valid? ::schess/fen fen)]}
   (js/Chess. fen)))

(def ^:const squares
  (into #{} (let [chess (create-chess)]
              (js->clj (.-SQUARES chess)))))

(defn- color [chess]
  {:post [(s/valid? ::schess/color %)]}
  (.turn chess))

(defn- chess-js->clj [move]
  (clojure.set/rename-keys move
                           {:color ::schess/color
                            :from ::schess/from
                            :to ::schess/to
                            :flags ::schess/flags
                            :piece ::schess/piece
                            :san ::schess/san
                            :fen ::schess/fen
                            :promotion ::schess/promotion}))

(defn- move [chess move]
  (chess-js->clj (js->clj (.move chess (clj->js move)) :keywordize-keys true)))

(s/fdef move
        :args (s/cat :chess any?
                     :move (s/or :san ::schess/san
                                 :default (s/keys :req-un [::schess/from
                                                           ::schess/to])))
        :ret (s/nilable ::schess/move))

(stest/instrument `move)

;; {"A2":["a3","a4"],"b2":["b3","b4"],"c2":["c3","c4"],"d2":["d3","d4"],"e2":["e3","e4"],"f2":["f3","f4"],"g2":["g3","g4"],"h2":["h3","h4"],"b1":["a3","c3"],"g1":["f3","h3"]}
(defn- moves [chess square]
  {:pre [(s/valid? ::sboard/square square)]
   :post [(s/valid? (s/nilable ::sboard/dests) %)]}
  (let [dests (map (fn [move]
                     ((js->clj move) "to"))
                   (.moves chess #js {:square square :verbose true}))]
    (when (not-empty dests) {square dests})))

(s/fdef moves
        :args (s/cat :chess any?
                     :square ::sboard/square)
        :ret ::sboard/dests)

(stest/instrument `moves)

(defn- dest-squares [chess]
  {:post [(s/valid? ::sboard/dests %)]}
  (reduce merge (map #(moves chess %1) squares)))

(defn compute-state [{:keys [::schess/initial-fen
                             ::schess/moves
                             ::schess/current-ply] :as state}]
  (let [fen (if (or (empty? moves)
                    (= current-ply 0))
              initial-fen
              (::schess/fen (nth moves (- current-ply 1))))
        last-move (when (> current-ply 0) (nth moves (- current-ply 1)))
        chess (create-chess fen)]
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

(def tmp1)

(defn make-move [{:keys [::schess/initial-fen
                         ::schess/moves
                         ::schess/current-ply] :as state} from to & {:keys [::schess/promotion] :as options}]
  {:post [(s/valid? ::schess/game %)]}
  (set! tmp1 state)
  (let [{fen ::schess/fen} (compute-state state)
        chess (create-chess fen)
        move-args (apply hash-map
                         `[:from ~from
                           :to ~to
                           ~@(when promotion (list :promotion promotion))])
        move (move chess move-args)
        move-info (assoc move ::schess/fen (.fen chess))]
    (-> state
        (update ::schess/moves #(conj % move-info))
        (update ::schess/current-ply inc))))

(s/fdef make-move
        :args (s/cat :state ::schess/game
                     :from ::sboard/square
                     :to ::sboard/square
                     :options (s/keys* :opt [::schess/promotion]))
        :ret ::schess/game)

(stest/instrument `make-move)


(defn- compute-fens [initial-fen moves]
  (let [chess (create-chess initial-fen)]
    (vec (for [move moves]
           (let [replayed-move (chess-js->clj (js->clj (.move chess (clj->js move)) :keywordize-keys true))]
             (assoc replayed-move ::schess/fen (.fen chess)))))))

(s/fdef compute-fens
        :args (s/cat :initial-fen ::schess/initial-fen
                     :moves ::schess/moves)
        :ret ::schess/moves)

(stest/instrument `make-move)


(defn load-pgn [{:keys [::schess/initial-fen
                        ::schess/moves
                        ::schess/current-ply] :as state} pgn]
  (let [chess (create-chess)
        initial-fen (.fen chess)]
    (.load_pgn chess pgn)
    (let [moves (map #'chess-js->clj
                     (js->clj (.history chess #js {:verbose true}) :keywordize-keys true))
          moves-and-fen (compute-fens initial-fen moves)]
      {::schess/moves moves-and-fen
       ::schess/current-ply 0
       ::schess/initial-fen initial-fen})))

(s/fdef load-pgn
        :args (s/cat :state ::schess/game
                     :pgn ::schess/pgn)
        :ret ::schess/game)

(stest/instrument `load-pgn)
