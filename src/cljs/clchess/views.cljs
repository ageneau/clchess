(ns clchess.views
  (:require [reagent.core  :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]
            [re-frame.core :refer [subscribe dispatch]]
            [clchess.utils :as utils]
            [clchess.board :as board]
            [clchess.theme :as theme]
            [clchess.widgets :as widgets]
            [uci.core :as uci]
            [taoensso.timbre :as log]))

(defn reset-button []
  [:input {:type "button" :value "Reset"
           :on-click #(board/reset-board)}])

(defn start-engine-button []
  [:input {:type "button" :value "Start engine"
           :on-click #(uci/run-engine)}])

(defn file-input [value]
  [:input {:field :file
           :type :file
           :accept ".pgn"
           :value @value
           :id "file-selector"
           :on-change (fn [val]
                        (let [file (-> val .-target .-value)]
                          (reset! value (-> val .-target .-value))
                          (board/load-pgn (utils/read-file file))))}])

(defn selected-file []
  (let [val (reagent/atom "None")]
    (fn []
      [:div
       [:p "Selected file: " @val]
       [file-input val]])))

(defn chessboard []
  [:div {:class "lichess_board_wrap cg-512"}
   [:div {:class "lichess_board standard"}
    [:div {:id "chessground-container"}]
    [widgets/study-overboard]]])

(defn top-menu []
  [:div {:class "hover" :id "topmenu"}
   [:section
    [:a "Play"]
    [:div
     [:a "Create a game"]
     [:a "Tournament"]
     [:a "Simultaneous exhibitions"]]]
   [:section
    [:a "Learn"]
    [:div
     [:a "Training"]
     [:a "Openings"]
     [:a "Coordinates"]]]])

(defn top-section [theme]
  (log/info "top section:" (:is-2d theme))
  [:div {:class (if (:is-2d theme) "is2d" "is3d") :id "top"}
   [top-menu]
   [widgets/hamburger]
   [theme/theme-selector theme]
   [widgets/volume-control]])

(defn controls []
  [:div {:id "controls"}
   [:input {:type "button" :value "back"}]
   [:input {:type "button" :value "next"
            :on-click #(board/next-move)}]
   [selected-file]
   [reset-button]
   [start-engine-button]])

(defn clchess-app []
  (let [theme (subscribe [:theme])]
    [:div {:id "page-container"}
     [top-section @theme]
     [:div {:class (if (:is-2d @theme) "is2d" "is3d") :id "content"}
      [:div {:class "lichess_game"}
       [chessboard]
       [:div {:class "lichess_ground"}
        [widgets/ceval-box]
        [widgets/opening-box]
        [widgets/replay]
        [widgets/explorer-box]
        [widgets/game-controls]]]]
     [widgets/tip]
     [widgets/context-menu]]))
