(ns clchess.css
    (:require [garden.def :refer [defstyles]]))

(defstyles clchess
  [:body {:background "#202020"
          :text-align "center"
          :color "#b0b0b0"
          :font-size "11px"
          :margin "30 px 0 0 0"}]
  [:section {:display "inline-block"
             :margin "0 5px 20px 0"
             :background "#404040"
             :padding "5px"
             :border-radius "2px"}]
  [:section [:p {:margin "5px 0 0 0"}]]
  [:.chessground.tiny {:width "225px"
                       :height "225px"}]
  [:.chessground.small {:width "300px"
                        :height "300px"}]
  [:.chessground.normal {:width "512px"
                         :height "512px"}]
  [:.cg-board-wrap [:svg {:opacity "0.6"
                          :overflow "hidden"
                          :position "relative"
                          :top "0px"
                          :left "0px"
                          :width "100%"
                          :height "100%"
                          :pointer-events "none"
                          :z-index "2"}]]
  [:.cg-board-wrap [:svg [:* {:transition "0.35s"}]]])
