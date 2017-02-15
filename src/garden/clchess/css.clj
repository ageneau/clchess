(ns clchess.css
  (:require [garden.def :refer [defstyles]]
            [garden.selectors :as s]
            [garden.units :as u]))

(defn type= [v]
  (s/attr :type := v))


(defstyles clchess
  [(s/input (type= "file")) {:display "none"}]

  [:div:-webkit-full-screen {:width "100% !important"
                             :height "100% !important"}]
  
  [:.chessground {:background "#404040"
                  :padding "20px"
                  :border-radius "2px"
                  :margin-left "auto"
                  :margin-right "auto"}]
  [:.chessground.tiny {:width "225px"
                       :height "225px"}]
  [:.chessground.small {:width "300px"
                        :height "300px"}]
  [:.chessground.normal {:width "600px"
                         :height "600px"}]
  [:.cg-board-wrap [:svg {:opacity "0.6"
                          :overflow "hidden"
                          :position "relative"
                          :top "0px"
                          :left "0px"
                          :width "100%"
                          :height "100%"
                          :pointer-events "none"
                          :z-index "2"}]]
  [:.cg-board-wrap [:svg [:* {:transition "0.35s"}]]]
  [:th :tr {:text-align "left"
            :overflow "hidden" }]
  [:#page-container {}]
  [:#content {:margin "35px auto 30px auto"}]
  [:#chessboard-and-controls {:float "left"
                              :text-align "center"
                              :display "inline-block"
                              :word-wrap "break-word"}]
  [:#controls {:width "600px"
               :max-height "250px"
               :margin-left "auto"
               :margin-right "auto"}]
  [:#game-list-container {:float "left"
                          :min-width "100px"
                          :max-width "600px"
                          :width "400px"
                          :overflow-x "auto"}]
  [:#game-list {:width "800px"}]
  [:th.date :th.result :th.length :th.player-name :th.player-elo {:min-width "20px"
                                                                  }]
  [:th.move-list {:min-width "100px"
                  }])
