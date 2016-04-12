(ns reagent2.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]))

(enable-console-print!)

(defn by-id [id]
  (.getElementById js/document (name id)))

(def chessground (by-id "ground7"))

(def chess (js/Chess.))
(def ground)

(def SQUARES (.-SQUARES chess))

(defn color []
  (if (= (.turn chess) "w") "white" "black"))

;; {"a2":["a3","a4"],"b2":["b3","b4"],"c2":["c3","c4"],"d2":["d3","d4"],"e2":["e3","e4"],"f2":["f3","f4"],"g2":["g3","g4"],"h2":["h3","h4"],"b1":["a3","c3"],"g1":["f3","h3"]}
(defn moves [square]
  (let [dests
        (map (fn [move] ((js->clj move) "to"))
             (.moves chess #js {:square square :verbose true}))]
    (when (not-empty dests) {square dests})))

(defn dest-squares []
  (clj->js (reduce merge (map moves SQUARES))))

(defn on-move [orig dest]
  (println "on-move")
  (.move chess #js {:from orig :to dest})
  (.set ground #js {:turnColor (color) :movable #js {:color (color) :dests (dest-squares)}})
  (println (.getFen ground)))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to Reagent"]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (let [params #js {
                    :viewOnly false
                    :turnColor "white"
                    :animation #js { :duration 500 }
                    :movable #js {
                                  :free false
                                  :color (color)
                                  :premove true
                                  :dests (dest-squares)
                                  :events #js { :after on-move }
                                  }
                    :drawable #js { :enabled true }
                    }
        ground2 (js/Chessground. chessground params)]
    (println "init params:" params);
    (set! ground ground2))
  (mount-root))
