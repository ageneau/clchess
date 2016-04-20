(ns clchess.board
    (:require [scid.core :as scid]
              [clchess.utils :as utils]))

(def chess (js/Chess.))
(def loaded-game (js/Chess.))
(def current-ply 0)

(def *chessground*)

(def SQUARES (.-SQUARES chess))

(defn color []
  (if (= (.turn chess) "w") "white" "black"))

;; {"a2":["a3","a4"],"b2":["b3","b4"],"c2":["c3","c4"],"d2":["d3","d4"],"e2":["e3","e4"],"f2":["f3","f4"],"g2":["g3","g4"],"h2":["h3","h4"],"b1":["a3","c3"],"g1":["f3","h3"]}
(defn moves [square]
  (let [dests
        (map (fn [move] ((js->clj move) "to"))
             (.moves chess #js {:square square :verbose true}))]
    (when (not-empty dests) {square dests})))

(defn dest-squares []
  (clj->js (reduce merge (map moves SQUARES))))

(defn on-move [orig dest]
  (println "on-move: " orig "->" dest)
  (.move chess #js {:from orig :to dest})
  (.set *chessground* #js {:turnColor (color) :movable #js {:color (color) :dests (dest-squares)}})
  (println "board fen:" (.getFen *chessground*)))

(defn init-board []
  (set! *chessground* (js/Chessground. (utils/by-id "chessground-container"))))

(defn reset-board []
  (println "reset-board")
  (.reset chess)
  (.set *chessground* #js {
                         :viewOnly false
                         :autoCastle true
                         :turnColor "white"
                         :fen "start"
                         :animation #js { :duration 500 }
                         :movable #js {
                                       :free false
                                       :color (color)
                                       :premove true
                                       :dests (dest-squares)
                                       :events #js { :after on-move }
                                       }
                         :drawable #js { :enabled true }
                         }))


(defn load-pgn [pgn]
  (println "load-pgn: " pgn)
  (set! current-ply 0)
  (.reset chess)
  (.reset loaded-game)
  (.load_pgn loaded-game pgn)
  (println "FEN: " (.fen loaded-game))
  (.set *chessground* #js {
                         :viewOnly true
                         :autoCastle true
                         :turnColor "white"
                         :fen "start"
                         :animation #js { :duration 500 }
                         :drawable #js { :enabled true }
                         }))

(defn next-move []
  (let [hist (.history loaded-game #js {:verbose true})
      move (js->clj (get hist current-ply))]
  (println "From: " (move "from") " to:" (move "to"))
  (if (= current-ply (dec (count hist)))
    (js/alert "End of game")
    (do
      (.move *chessground* (move "from") (move "to"))
      (set! current-ply (inc current-ply))))))
