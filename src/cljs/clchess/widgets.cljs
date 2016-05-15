(ns clchess.widgets
    (:require [reagent.core :as reagent :refer [atom]]
              [goog.dom :as dom]
              [goog.dom.classlist :as classlist]
              [goog.events :as events]
              [clojure.string :as string]
              [clchess.utils :as utils]
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

(defn explorer-box []
  [:div {:class "explorer_box"}
   ;; [spinner]
   [:div {:class "data"}
    [:table {:class "moves"}
     [:thead>tr
      [:th "Move"]
      [:th "Games"]
      [:th "White / Draw / Black"]]
     [:tbody
      [:tr {:data-uci "b1c3" :title "Average rating: 2489"}
       [:td "Nc3"]
       [:td "358"]
       [:td [:div {:class "bar"}
             [:span {:class "white" :style {:width "39.4%"}} "39%"]
             [:span {:class "draws" :style {:width "34.4%"}} "34%"]
             [:span {:class "black" :style {:width "26.3%"}} "26%"]]]]]]
    [:table {:class "games"}
     [:thead>tr
      [:th {:colSpan "4"} "top games"]]
     [:tbody
      [:tr
       [:td [:span "2778"] [:span "2843"]]
       [:td [:span "Karjakin, Sergey"][:span "Carlsen, Magnus"]]
       [:td [:result {:class "draws"} "½-½"]]
       [:td "2012"]]
      [:tr
       [:td [:span "2773"] [:span "2843"]]
       [:td [:span "Karjakin, Sergey"][:span "Carlsen, Magnus"]]
       [:td [:result {:class "white"} "1-0"]]
       [:td "2012"]]]]]])

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
