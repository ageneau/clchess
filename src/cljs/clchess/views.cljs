(ns clchess.views
  (:require [reagent.core  :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]
            [clchess.utils :as utils]
            [clchess.board :as board]
            [clchess.theme :as theme]
            [clchess.widgets :as widgets]
            [uci.core :as uci]
            [taoensso.timbre :as log]))


(defn start-engine-button []
  [:input {:type "button" :value "Start engine"
           :on-click #(uci/run-engine)}])

(defn file-input []
  (let [value (reagent/atom "")]
    [:input {:field :file
             :type :file
             :accept ".pgn"
             :id "file-selector"
             :value @value
             :on-click #(reset! value "")
             :on-change #(dispatch [:file/changed (-> % .-target .-value)])
             }]))

(defn study-overboard []
  [:div.lichess_overboard.study_overboard
   [:a.close.icon {:data-icon "L"}]
   [:h2 "Edit study"]
   [:form.material.form
    [:div.game.form-group
     [:input#study-name
      {:required "", :minlength "3", :maxlength "100"}]
     [:label.control-label {:for "study-name"} "Name"]
     [:i.bar]]
    [:div.game.form-group
     [:select#study-visibility
      [:option {:value "public"} "Public"]
      [:option {:value "private"} "Invite only"]]
     [:label.control-label
      {:for "study-visibility"}
      "Visibility"]
     [:i.bar]]
    [:div
     [:div.game.form-group.half
      [:select#study-computer
       [:option {:value "everyone"} "Everyone"]
       [:option {:value "nobody"} "Nobody"]
       [:option {:value "owner"} "Only me"]
       [:option {:value "contributor"} "Contributors"]]
      [:label.control-label
       {:for "study-computer"}
       "Computer analysis"]
      [:i.bar]]
     [:div.game.form-group.half
      [:select#study-explorer
       [:option {:value "everyone"} "Everyone"]
       [:option {:value "nobody"} "Nobody"]
       [:option {:value "owner"} "Only me"]
       [:option {:value "contributor"} "Contributors"]]
      [:label.control-label
       {:for "study-explorer"}
       "Opening explorer"]
      [:i.bar]]]
    [:div.button-container
     [:button.submit.button {:type "submit"} "Save"]]]
   [:form.delete_study
    {:action "/study/JsKHdGfK/delete", :method "post"}
    [:button.button.frameless "Delete study"]]])

(defn chessboard []
  [:div {:class "lichess_board_wrap cg-512"}
   [:div {:class "lichess_board standard"}
    [:div {:id "chessground-container"}]
    ;; [study-overboard]
    ]])

(defn top-menu []
  [:div {:class "hover" :id "topmenu"}
   [:section
    [:a "File"]
    [:div
     [:a {:on-click #(dispatch [:menu/open-db])} "Open database"]
     [:a {:on-click #(dispatch [:menu/load-pgn])} "Load pgn"]]]
   [:section
    [:a "Board"]
    [:div
     [:a {:on-click #(dispatch [:menu/reset-board])} "Reset board"]]]])

(defn top-section [theme]
  (log/debug "top section:" (:is-2d theme))
  [:div {:class (if (:is-2d theme) "is2d" "is3d") :id "top"}
   [top-menu]
   [widgets/hamburger]
   [theme/theme-selector theme]
   [widgets/volume-control 80 false]])

(defn clchess-app []
  (let [theme (subscribe [:theme])
        moves (subscribe [:game/moves])]
    [:div {:id "page-container"}
     [top-section @theme]
     [:div {:class (if (:is-2d @theme) "is2d" "is3d") :id "content"}
      [:div {:class "lichess_game"}
       [board/board-outer]
       ;;[chessboard]
       [:div {:class "lichess_ground"}
        [widgets/ceval-box]
        [widgets/opening-box]
        [widgets/replay @moves]
        [widgets/explorer-box]
        [widgets/game-controls]]]]
     [file-input]
     ;; [widgets/tip]
     ;; [widgets/context-menu]
     ]))
