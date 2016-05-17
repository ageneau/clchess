(ns clchess.board
    (:require [scid.core :as scid]
              [clchess.utils :as utils]
              [taoensso.timbre :as log]
              [reagent.core :as reagent :refer [atom]]
              [clchess.widgets :as widgets]
              [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

;; https://github.com/Day8/re-frame/wiki/Using-Stateful-JS-Components
(defn board-inner []
  (let [board (atom nil)
        options (clj->js {"zoom" 9})
        update  (fn [comp]
                  (let [{:keys [board-state]} (reagent/props comp)
                        chessground (:chessground @board)
                        movable (fn [origin dest metadata]
                                  (log/debug "Origin:" origin "," dest "," metadata)
                                  (dispatch [:game/board-move origin dest]))
                        options {
                                 :autoCastle true
                                 :animation {:duration 300 }
                                 :drawable {:enabled true }
                                 :highlight {:lastMove true
                                             :check true
                                             :dragOver true}
                                 }
                        new-state (-> board-state
                                      (merge options)
                                      (assoc-in [:movable :events :after] movable))]
                    (log/debug "Board state:" board-state)
                    (log/debug "NEW state:" new-state)
                    (.set chessground (clj->js new-state))))]

    (reagent/create-class
      {:reagent-render (fn []
                         [:div.lichess_board_wrap {:class "cg-512"}
                          [:div.lichess_board {:class "standard"}
                           [:div {:id "chessground-container"}]
                           ;; [widgets/promotion-choice "f8" "white"]
                           ]])

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
