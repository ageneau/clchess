(ns clchess.board
    (:require [clchess.utils :as utils]
              [taoensso.timbre :as log]
              [reagent.core :as reagent :refer [atom]]
              [clchess.widgets :as widgets]
              [clojure.string :as string]
              [re-frame.core :refer [subscribe dispatch dispatch-sync]]
              [clchess.specs.board :as sboard]
              [cljsjs.chessground]))

(defn opposite [player]
  (case player
    "black" "white"
    "white" "black"))

(defn to-pos [square]
  (let [[file row] square
        row-pos (- (utils/char-code row) (utils/char-code "1"))
        file-pos (- (utils/char-code file) (utils/char-code "a"))]
    [row-pos file-pos]))

(defn to-square [pos]
  (let [[file-pos row-pos] pos
        row (char (+ row-pos (utils/char-code "a")))
        file (char (+ file-pos (utils/char-code "1")))]
    (str row file)))

(defn row [square]
  (second square))

(defn file [square]
  (first square))

(defn square-pos [square]
  (let [[row-pos file-pos] (to-pos square)]
    {:top (utils/percent-string (* (- 7 row-pos) 12.5))
     :left (utils/percent-string (* file-pos 12.5)) }))

(defn piece-to-db-format [piece]
  (case piece
    "pawn" "p"
    "queen" "q"
    "knight" "n"
    "rook" "r"
    "bishop" "b"))

(def ^:const promotion-pieces ["queen" "knight" "rook" "bishop"])

(defn adjacent-square [square direction nsquares]
  (let [[row-pos file-pos] (to-pos square)]
    (case direction
      :right (to-square [row-pos (+ file-pos nsquares)])
      :left (to-square [row-pos (- file-pos nsquares)])
      :above (to-square [(+ row-pos nsquares) file-pos])
      :below (to-square [(- row-pos nsquares) file-pos]))))


(defn promotion-choice [promotion-square color]
  (let [vpos (case color
               "white" "top"
               "black" "bottom")
        [row-pos _] (to-pos promotion-square)
        direction (case row-pos
                    0 :above
                    7 :below)
        squares (map #(adjacent-square promotion-square
                                       direction
                                       %)
                     (range (count promotion-pieces)))]
    (into [:div#promotion_choice {:class vpos }]
          (map (fn [piece square]
                 [:square
                  {:style (square-pos square)
                   :on-click #(dispatch [:game/promote-to (piece-to-db-format piece)])}
                  [:piece { :class (string/join " " [piece color]) }]])
               promotion-pieces
               squares))))

(defn- is-promoting [dest piece turn]
  (let [dest-row (row dest)]
    (and (= (:role piece) "pawn")
         (or (and (= dest-row "8") (= turn "white"))
             (and (= dest-row "1") (= turn "black"))))))

(defn on-board-move [chessground]
  (fn [origin dest metadata]
    (let [piece (js->clj (-> chessground
                             .-data
                             .-pieces
                             (aget dest)) :keywordize-keys true)
          turn (js->clj (-> chessground
                            .-data
                            .-turnColor))
          promoting (is-promoting dest piece turn)
          player (opposite turn)]
      (log/debug "Origin:" origin ","
                 dest ","
                 (js->clj metadata) ","
                 piece ","
                 turn ","
                 promoting)
      (dispatch [:game/board-move
                 origin
                 dest
                 { :promoting promoting :player turn}]))))

;; https://github.com/Day8/re-frame/blob/master/docs/Using-Stateful-JS-Components.md
(defn board-inner []
  (let [board (atom nil)
        options (clj->js {"zoom" 9})
        update  (fn [comp]
                  (let [{:keys [board-state]} (reagent/props comp)
                        chessground (:chessground @board)
                        movable (on-board-move chessground)
                        options {
                                 :autoCastle true
                                 :animation {:duration 300 }
                                 :drawable {:enabled true }
                                 :highlight {:lastMove true
                                             :check true
                                             :dragOver true}
                                 :resizable true ;; listens to chessground.resize on document.body to clear bounds cache
                                 }
                        new-state (-> board-state
                                      (merge options)
                                      (assoc-in [:movable :events :after] movable))]
                    (.set chessground (clj->js new-state))))]

    (reagent/create-class
     {:reagent-render (fn [promotion]
                        (fn [{{{show :show
                                to :to
                                player :player} :promotion} :board-state}]
                          (log/debug "In render:" player)
                          [:div.lichess_board_wrap {:class "cg-512"}
                            `[:div.lichess_board {:class "standard"}
                              [:div {:id "chessground-container"}]
                              ~@(when show (list [promotion-choice to player]))]]))

      :component-did-mount (fn [comp]
                             (let [container (utils/by-id "chessground-container")
                                   chessground (js/Chessground. container)]
                               (log/debug "Component did mount:" comp ", " chessground)
                               (reset! board {:chessground chessground}))
                             (update comp))

      :component-did-update update
      :display-name "board-inner"})))

(defn board-outer []
  (let [board (subscribe [:board])]   ;; obtain the data
    (fn []
      [board-inner {:board-state @board}])))


(defn force-resize []
  (.dispatchEvent (utils/body) (js/Event. "chessground.resize")))
