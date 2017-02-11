(ns clchess.ctrl
  (:require [clchess.utils :as utils]
            [taoensso.timbre :as log]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(defprotocol ChessGame
  (clear [this])
  (fen [this])
  (game-over? [this])
  (history [this])
  (in-check? [this])
  (in-checkmate? [this])
  (in-draw? [this])
  (dests [this fen])
  (move [this orig dest]))

(def ^:const squares
  (into #{} (let [chess (js/Chess.)]
              (js->clj (.-SQUARES chess)))))

(defn ^:private color [chess]
  (if (= (.turn chess) "w") "white" "black"))

;; {"A2":["a3","a4"],"b2":["b3","b4"],"c2":["c3","c4"],"d2":["d3","d4"],"e2":["e3","e4"],"f2":["f3","f4"],"g2":["g3","g4"],"h2":["h3","h4"],"b1":["a3","c3"],"g1":["f3","h3"]}
(defn ^:private moves [chess square]
  (let [dests (map (fn [move]
                     ((js->clj move) "to"))
                   (.moves chess #js {:square square :verbose true}))]
    (when (not-empty dests) {square dests})))

(defn ^:private dest-squares [chess]
  (reduce merge (map #(moves chess %1) squares)))

(defn compute-state [{:keys [initial-fen moves current-ply] :as state}]
  (let [fen (if (or (empty? moves)
                    (= current-ply 0))
              initial-fen
              (:fen (nth moves (- current-ply 1))))
        last-move (when (> current-ply 0) (nth moves (- current-ply 1)))
        chess (js/Chess. fen)]
    {:fen fen
     :color (color chess)
     :last-move last-move
     :dest-squares (dest-squares chess)}))

(defn make-move [{:keys [initial-fen moves current-ply] :as state} from to & {:keys [promotion] :as options}]
  (let [{fen :fen} (compute-state state)
        chess (js/Chess. fen)
        move-args (apply hash-map
                         `[:from ~from
                           :to ~to
                           ~@(when promotion (list :promotion promotion))])
        move (js->clj (.move chess (clj->js move-args)) :keywordize-keys true)
        move-info (assoc move :fen (.fen chess))]
    (log/debug "make-move:" promotion ", " move-args ":: " (conj moves move-info))
    {:moves (conj moves move-info)
     :current-ply (inc current-ply)}))

(defn compute-fens [initial-fen moves]
  (log/debug "compute-fens: " initial-fen ", " moves)
  (let [chess (js/Chess. initial-fen)]
    (vec (for [move moves]
           (let [replayed-move (js->clj (.move chess (clj->js move)) :keywordize-keys true)]
             (log/debug "replayed: " replayed-move)
             (assoc replayed-move :fen (.fen chess)))))))

(defn load-pgn [{:keys [initial-fen moves current-ply] :as state} pgn]
  (let [chess (js/Chess.)
        initial-fen (.fen chess)]
    (.load_pgn chess pgn)
    (let [moves (js->clj (.history chess #js {:verbose true}) :keywordize-keys true)
          moves-and-fen (compute-fens initial-fen moves)]
      (log/debug "load-pgn: " pgn ", " moves-and-fen)
      {:moves moves-and-fen
       :current-ply 0
       :initial-fen initial-fen})))
