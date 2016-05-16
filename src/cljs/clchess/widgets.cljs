(ns clchess.widgets
  (:require [reagent.core :as reagent :refer [atom]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classlist]
            [goog.events :as events]
            [clojure.string :as string]
            [clchess.utils :as utils :refer [percent-string]]
            [taoensso.timbre :as log]))


(defn hamburger []
  [:div {:id "ham-plate" :class "fright link hint--bottom" :data-hint "Menu"}
   [:div {:id "hamburger" :data-icon "["}]])

(defn slider [value]
  [:div {:class "zoom-control"}
   [:i {:data-icon "<"}]
   [:div {:class "slider ui-slider ui-slider-horizontal ui-widget ui-widget-content ui-corner-all"}
    [:div {:class "ui-slider-range ui-widget-header ui-corner-all ui-slider-range-min" :style { :width value}}]
    [:span {:class "ui-slider-handle ui-state-default ui-corner-all" :tabindex "0" :style { :left value }}]]])

;; (defn slider [param value min max]
;;   [:input {:type "range" :value value :min min :max max
;;            :style {:width "100%"}
;;            :on-change (fn [e]
;;                         (swap! app-state assoc param (.-target.value e)))}])

(defn ceval-box []
  [:div {:class "ceval_box"}
   [:div {:class "switch"}
    [:input {:id "analyse-toggle-ceval" :class "cmn-toggle cmn-toggle-round" :type "checkbox"}]
    [:label {:for "analyse-toggle-ceval"}]]
   [:help "Local computer evaluation<br>for variation analysis"]])

(defn opening-box []
  [:div {:class "opening_box" :title "B00 King's Pawn"}
   [:strong "B00"]
   " King's Pawn"])

(defn replay []
  [:div {:class "replay"}
   [:turn [:index "1"] [:move "e4"] [:move "e6"]]
   [:turn [:index "2"] [:move "d4"] [:move {:class "active"} "d5"]]])

(defn spinner []
  [:div {:class "spinner"}
   [:svg {:viewBox "0 0 40 40"}
    [:circle {:cx "20" :cy "20" :r "18" :fill "none"}]]])

(defn move-row [{:keys [move games white draw black rating]}]
  [:tr {;; :data-uci "b1c3"
        :title (str "Average rating: " rating)}
   [:td move]
   [:td games]
   [:td [:div {:class "bar"}
         [:span {:class "white" :style {:width (percent-string white)}} (percent-string white :round true)]
         [:span {:class "draws" :style {:width (percent-string draw)}} (percent-string draw :round true)]
         [:span {:class "black" :style {:width (percent-string black)}} (percent-string black :round true)]]]])

(defn move-table []
  [:table {:class "moves"}
   [:thead>tr
    [:th "Move"]
    [:th "Games"]
    [:th "White / Draw / Black"]]
   [:tbody
    [move-row {:move "Nc3" :games 358 :white 39.4 :draw 34.4 :black 26.3 :rating 2489}]
    [move-row {:move "Nd2" :games 244 :white 37.6 :draw 40 :black 22.4 :rating 2457}]]])

(defn printable-result [result]
  (case result
    "1-0" "1-0"
    "0-1" "0-1"
    "=-=" "½-½"))

(defn result-class [result]
  (case result
    "1-0" "white"
    "0-1" "black"
    "=-=" "draws"))

(defn game-row [{:keys [date result wplayer bplayer welo belo]}]
  [:tr
   [:td [:span welo] [:span belo]]
   [:td [:span wplayer][:span bplayer]]
   [:td [:result {:class (result-class result)} (printable-result result)]]
   [:td date]])

(defn game-list []
  [:table {:class "games"}
   [:thead>tr
    [:th {:colSpan "4"} "top games"]]
   [:tbody
    (for [game (scid.core/game-list)]
      ^ { :key (:id game)}
      [game-row game])]])

(defn explorer-box []
  [:div {:class "explorer_box"}
   ;; [spinner]
   [:div {:class "data"}
    [move-table]
    [game-list]]])

(defn context-menu []
  [:div#analyse-cm.visible
   {:style { :left "910px" :top "265px" }}
   [:div
    [:p.title "4... exd5"]
    [:a.action {:data-icon "E"} "Promote to main line"]
    [:a.action {:data-icon "q"} "Delete from here"]
    [:a.action {:data-icon "c"} "Comment this move"]
    [:a.action.glyph-icon "Annotate with symbols"]]])

(defn tip []
  [:div.hopscotch-bubble.animated
   {:style { :position "absolute" :top "530px" :left "609.219px" }}
   [:div.hopscotch-bubble-container
    {:style { :width "280px" :padding "15px" }}
    [:span.hopscotch-bubble-number "1"]
    [:div.hopscotch-bubble-content
     [:h3.hopscotch-title "New feature!"]
     [:div.hopscotch-content
      "Click this button to enable"
      [:br]
      [:strong "lichess opening explorer"]
      "."
      [:br]
      [:a
       {:href
        "http://lichess.org/blog/Vs0xMTAAAD4We4Ey/opening-explorer"}
       "Learn more about it"]]]
    [:div.hopscotch-actions
     [:button.hopscotch-nav-button.next.hopscotch-next "OK, got it"]]
    [:button.hopscotch-bubble-close.hopscotch-close "Close"]]
   [:div.hopscotch-bubble-arrow-container.hopscotch-arrow.right
    [:div.hopscotch-bubble-arrow-border]
    [:div.hopscotch-bubble-arrow]]])

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

(defn volume-control []
  (let [shown (reagent/atom false)]
    (fn []
      (let [is-shown @shown]
        [:div#sound_control.fright {:class (if is-shown "shown" "")}
         [:a#sound_state.toggle.link.hint--bottom-left
          {:data-hint "Sound"
           :on-click #(utils/handler-fn
                       (log/info "toggle")
                       (reset! shown (not is-shown)))}
          [:span.is2.on {:data-icon "#"}]
          [:span.is2.off {:data-icon "$"}]]
         [:div.dropdown
          [:div.slider.ui-slider.ui-slider-vertical.ui-widget.ui-widget-content.ui-corner-all
           [:div.ui-slider-range.ui-widget-header.ui-corner-all.ui-slider-range-min
            {:style { :height "70%" }}]
           [:span.ui-slider-handle.ui-state-default.ui-corner-all
            {:tabindex "0", :style { :bottom "70%"}}]]
          [:form.selector
           {:action "/pref/soundSet"}
           [:div.silent
            [:input#soundSet_silent.active
             {:checked "",
              :type "radio",
              :value "silent",
              :name "soundSet"}]
            [:label {:for "soundSet_silent"} "Silent"]]
           [:div.standard
            [:input#soundSet_standard
             {:type "radio", :value "standard", :name "soundSet"}]
            [:label {:for "soundSet_standard"} "Standard"]]
           [:div.piano
            [:input#soundSet_piano
             {:type "radio", :value "piano", :name "soundSet"}]
            [:label {:for "soundSet_piano"} "Piano"]]
           [:div.nes
            [:input#soundSet_nes
             {:type "radio", :value "nes", :name "soundSet"}]
            [:label {:for "soundSet_nes"} "NES"]]
           [:div.sfx
            [:input#soundSet_sfx
             {:type "radio", :value "sfx", :name "soundSet"}]
            [:label {:for "soundSet_sfx"} "SFX"]]
           [:div.futuristic
            [:input#soundSet_futuristic
             {:type "radio", :value "futuristic", :name "soundSet"}]
            [:label {:for "soundSet_futuristic"} "Futuristic"]]
           [:div.robot
            [:input#soundSet_robot
             {:type "radio", :value "robot", :name "soundSet"}]
            [:label {:for "soundSet_robot"} "Robot"]]]]]))))

(defn game-controls []
  [:div {:class "game_control"}
   [:div {:class "buttons"}
    [:div
     [:div {:class "jumps"}
      [:button {:class "button" :data-icon "Y"}]
      [:button {:class "first" :data-icon "W"}]]
     [:div {:class "jumps"}
      [:button {:class "button" :data-icon "X"}]
      [:button {:class "first" :data-icon "V"}]]]
    [:div
     [:button {:id "open_explorer" :data-hint "openingExplorer" :class "button hint--bottom active"}
      [:i {:data-icon "]"}]]
     [:button {:data-hint "Menu" :class "button hint--bottom"}
      [:i {:data-icon "["}]]]]])

(defn simple-toggle [options {:keys [on-toggle container-class initial-value]}]
  (let [state (reagent/atom initial-value)]
    (fn [options {:keys [on-toggle container-class button-class]}]
      (let [current-state @state]
        [:div {:class (if (string? container-class)
                        (string/join " " ["toggles" container-class])
                        "toggles")}
         (for [option options]
           ^{:key (option :name)}
           [:a {:class (string/join " " `[(option :name)
                                          "button"
                                          ~@(when (= current-state option) '("active"))])
                :data-icon (option :icon)
                :on-click #(utils/handler-fn
                            (reset! state option)
                            (when (fn? on-toggle)
                              (on-toggle option)))}
            (when (string? (option :text)) (option :text))])]))))
