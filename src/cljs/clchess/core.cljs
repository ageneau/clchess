(ns clchess.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.dom :as dom]
              [goog.dom.classlist :as classlist]
              [goog.events :as events]
              [node-webkit.core :as nw]
              [clojure.string :as string]
              [clchess.utils :as utils]
              [clchess.board :as board]
              [clchess.theme :as theme]
              [clchess.widgets :as widgets]
              [uci.core :as uci]
              [taoensso.timbre :as timbre]))

(enable-console-print!)

;; -------------------------
;; Views

(defn reset-button []
  [:input {:type "button" :value "Reset"
           :on-click #(board/reset-board)}])

(defn start-engine-button []
  [:input {:type "button" :value "Start engine"
           :on-click #(uci/run-engine)}])

(def app-state
  (reagent/atom
   {:games (scid.core/game-list)
    :is-2d true
    :theme "dark"
    :data-theme "brown"
    :zoom "80%"}))

(defn update-games! [f & args]
  (apply swap! app-state update-in [:games] f args))

(defn display-game-info [{:keys [date result length wplayer bplayer welo belo move] :as game}]
  (str date ", " wplayer ", " bplayer))

(defn game [{:keys [id date result length wplayer bplayer welo belo move] :as game}]
  [:tr
   {:on-click #(do
                (js/alert (str "Selected game: " id))
                (js/alert (str "Selected game: " wplayer " - " bplayer)))}
   [:td date]
   [:td result]
   [:td length]
   [:td wplayer]
   [:td welo]
   [:td bplayer]
   [:td belo]
   [:td move]])

(defn game-list []
  [:table {:id "game-list"}
   [:thead>tr
    [:th {:class "date"} "Date"]
    [:th {:class "result"} "Result"]
    [:th {:class "length"} "Length"]
    [:th {:class "player-name"} "White"]
    [:th {:class "player-elo"} "W-Elo"]
    [:th {:class "player-name"} "Black"]
    [:th {:class "player-elo"} "B-Elo"]
    [:th {:class "move-list"} "Move"]]

   [:tbody
    (for [c (:games @app-state)]
      ^{:key (:id c)}
      [game c])]])

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
    [:div {:id "chessground-container"}]]])

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

(defn top-section []
  [:div {:class (if (@theme/theme-state :is-2d) "is2d" "is3d") :id "top"}
   [top-menu]
   [widgets/hamburger]
   [theme/theme-selector]
   ])

(defn controls []
  [:div {:id "controls"}
   [:input {:type "button" :value "back"}]
   [:input {:type "button" :value "next"
            :on-click #(board/next-move)}]
   [selected-file]
   [reset-button]
   [start-engine-button]])

(defn home-page []
  [:div {:id "page-container"}
   [top-section]
   [:div {:class (if (@theme/theme-state :is-2d) "is2d" "is3d") :id "content"}
    [:div {:class "lichess_game"}
     [chessboard]
     [:div {:class "lichess_ground"}
      [widgets/ceval-box]
      [widgets/opening-box]
      [widgets/replay]
      [widgets/explorer-box]
      [widgets/game-controls]]]]
   [:div {:id "chessboard-and-controls"}
    ;; [chessboard]
    ;; [controls]
    ]
   ;; [:div {:id "game-list-container"} [game-list]]
   ])

;; -------------------------
;; Initialize app
;; -------------------------

(defn mount-root []
  (reagent/render [home-page] (utils/by-id "app")))

(defn reset-page []
  (mount-root)
  (board/init-board)
  (board/reset-board))

(defn init! []
  ;; (nw/menubar! [{:label "File"
  ;;                :submenu (nw/menu [{:label "Open"
  ;;                                    :click #(let [selector (utils/by-id "file-selector")]
  ;;                                              (.click selector))}
  ;;                                   {:label "Quit"
  ;;                                    :click nw/quit}])}])
  (theme/init-theme)
  (mount-root)
  (board/init-board)
  (board/reset-board))
