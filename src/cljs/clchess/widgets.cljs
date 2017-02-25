(ns clchess.widgets
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch subscribe]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classlist]
            [goog.events :as events]
            [clojure.string :as string]
            [clchess.utils :as utils :refer [percent-string]]
            [clchess.specs.chess :as schess]
            [clchess.specs.board :as sboard]
            [taoensso.timbre :as log]))

(defn hamburger []
  [:div {:id "ham-plate" :class "fright link hint--bottom" :data-hint "Menu"}
   [:div {:id "hamburger" :data-icon "["}]])

(defn slider [value]
  [:div {:class "zoom-control"}
   [:i {:data-icon "<"}]
   [:div {:class "slider ui-slider ui-slider-horizontal ui-widget ui-widget-content ui-corner-all"}
    [:div {:class "ui-slider-range ui-widget-header ui-corner-all ui-slider-range-min" :style { :width value}}]
    [:span {:class "ui-slider-handle ui-state-default ui-corner-all" :tabIndex "0" :style { :left value }}]]])

(defn vertical-slider [value]
  [:div.slider.ui-slider.ui-slider-vertical.ui-widget.ui-widget-content.ui-corner-all
   [:div.ui-slider-range.ui-widget-header.ui-corner-all.ui-slider-range-min
    {:style { :height (utils/percent-string value)}}]
   [:span.ui-slider-handle.ui-state-default.ui-corner-all
    {:tabIndex "0", :style { :bottom (utils/percent-string value)}}]])

;; (defn slider [param value min max]
;;   [:input {:type "range" :value value :min min :max max
;;            :style {:width "100%"}
;;            :on-change (fn [e]
;;                         (swap! app-state assoc param (.-target.value e)))}])

(defn ceval-box []
  [:div.ceval_box
   [:pearl [:span.cpu "CPU"]]
   [:help
    "Stockfish 8"
    [:span.native "native"]
    [:br]
    "in local browser"]
   [:div.switch
    {:title "Toggle local evaluation (l)"}
    [:input#analyse-toggle-ceval.cmn-toggle.cmn-toggle-round
     {:type "checkbox"}]
    [:label {:for "analyse-toggle-ceval"}]]
   [:a.show-threat {:data-icon "7", :title "Show threat (x)"}]])

(defn opening-box []
  [:div.opening_box
   {:title
    "D31 Queen's Gambit Declined: Queen's Knight Variation"}
   [:strong "D31"]
   " Queen's Gambit Declined: Queen's Knight Variation"])

(defn ^:private group-moves-by-color [moves]
  (let [start-color (::schess/color (first moves))
        last-color (::schess/color (last moves))
        partitionable `[~@(when-not (= start-color "w") '(:empty-move))
                        ~@moves
                        ~@(when-not (= last-color "b") '(:empty-move))]]
    (partition 2 partitionable)))

(defn replay [moves]
  (fn [moves]
    (let [grouped (group-moves-by-color moves)]
      (log/debug "REPLAY: " moves)
      (into [:div {:class "replay"}]
            (for [i (range (count grouped))]
              (let [[wmove bmove] (nth grouped i)
                    key (str "replay_" i)]
                ^{ :key key }
                `[:turn [:index ~(str (+ i 1))]
                  ~@(when-not (= wmove :empty-move) (list [::schess/move (::schess/san wmove)]))
                  ~@(when-not (= bmove :empty-move) (list [::schess/move (::schess/san bmove)]))]))))))

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
  (let [games (subscribe [:game/list])]
    [:table {:class "games"}
     [:thead>tr
      [:th {:colSpan "4"} "top games"]]
     [:tbody
      (for [game @games]
        ^ { :key (:id game)}
        [game-row game])]]))

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

(def soundset
  [{:key "silent"
    :text "Silent"
    :active true }
   {:key "piano"
    :text "Piano"
    :active false }
   {:key "nes"
    :text "NES"
    :active false }
   {:key "sfx"
    :text "SFX"
    :active false }
   {:key "futuristic"
    :text "Futuristic"
    :active false }
   {:key "robot"
    :text "Robot"
    :active false }])

(defn volume-control [volume is-on]
  (let [shown (reagent/atom false)]
    (fn [volume]
      (let [is-shown @shown]
        [:div#sound_control.fright {:class (if is-shown "shown" "")}
         [:a#sound_state.toggle.link.hint--bottom-left
          {:data-hint "Sound"
           :on-click #(utils/handler-fn
                       (log/debug "toggle")
                       (reset! shown (not is-shown)))}
          [:span.is2 {:class (if is-on "off" "on") :data-icon "#"}]
          [:span.is2 {:class (if is-on "on" "off") :data-icon "$"}]]
         [:div.dropdown
          [vertical-slider volume]
          [:form.selector
           {:action "/pref/soundSet"}
           (for [{:keys [key text active]} soundset]
             (let [class (string/join "_" ["soundSet" key])]
               ^{:key key}
               [:div {:class key }
                [:input
                 {:class (if active (string/join " " [class "active"]) class)
                  :checked ""
                  :type "radio"
                  :value key
                  :name "soundSet"}]
                [:label {:for class } text]]))]]]))))

(defn game-controls []
  [:div.game_control
   [:div.features
    [:button.hint--bottom
     {:data-hint "Opening explorer & tablebase",
      :data-act "explorer"}
     [:i {:data-icon "]"}]]
    [:button.hint--bottom
     {:data-hint "Practice with computer", :data-act "practice"}
     [:i {:data-icon ""}]]]
   [:div.jumps
    [:button {:data-act "first", :data-icon "W" :on-click #(dispatch [:game/first-move])}]
    [:button {:data-act "prev", :data-icon "Y" :on-click #(dispatch [:game/previous-move])}]
    [:button {:data-act "next", :data-icon "X", :on-click #(dispatch [:game/next-move])}]
    [:button {:data-act "last", :data-icon "V", :on-click #(dispatch [:game/last-move])}]]
   [:button.hint--bottom
    {:data-hint "Menu", :data-act "menu"}
    [:i {:data-icon "["}]]])

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
