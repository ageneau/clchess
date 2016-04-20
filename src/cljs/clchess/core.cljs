(ns clchess.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [node-webkit.core :as nw]
              [clojure.string :as string]
              [scid.core :as scid]
              [clchess.utils :as utils]
              [clchess.board :as board]))

(enable-console-print!)

;; -------------------------
;; Views

(defn reset-button []
  [:input {:type "button" :value "Reset"
           :on-click #(board/reset-board)}])

(def app-state
  (reagent/atom
   {:games
    (scid.core/game-list)}))

(defn update-games! [f & args]
  (apply swap! app-state update-in [:games] f args))

(defn display-game-info [{:keys [date result length wplayer bplayer welo belo move] :as game}]
  (str date ", " wplayer ", " bplayer))

(defn game [{:keys [date result length wplayer bplayer welo belo move] :as game}]
  [:tr
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
   [:thead
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
  [:div {:class "chessground normal wood cburnett" :id "chessground-container"}])

(defn controls []
  [:div {:id "controls"}
    [:input {:type "button" :value "back"}]
    [:input {:type "button" :value "next"
             :on-click board/next-move}]
    [selected-file]
    [reset-button]])

(defn home-page []
  [:div {:id "page-container"}
   [:div {:id "chessboard-and-controls"}
    [chessboard]
    [controls]]
   [:div {:id "game-list-container"} [game-list]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [home-page] (utils/by-id "app")))

(defn init! []
  (nw/menubar! [{:label "File"
                 :submenu (nw/menu [{:label "Open"
                                     :click #(let [selector (utils/by-id "file-selector")]
                                               (.click selector))}
                                    {:label "Quit"
                                     :click nw/quit}])}])
  (mount-root)
  (board/init-board)
  (board/reset-board))
