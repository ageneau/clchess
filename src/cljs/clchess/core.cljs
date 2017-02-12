(ns clchess.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [secretary.core :as secretary]
            [goog.events :as events]
            [clchess.utils :as utils]
            [clchess.board :as board]
            [clchess.events] ;; Do not remove this. This is needed for handler registration
            [clchess.subs] ;; Do not remove this. This is needed for subscription handler registration
            [clchess.views]
            [clchess.theme :as theme]
            [taoensso.timbre :as log])
  (:import [goog History]
           [goog.history EventType]))

;; -- Routes and History ------------------------------------------------------

;; (defroute "/" [] (dispatch [:set-showing :all]))
;; (defroute "/:filter" [filter] (dispatch [:set-showing (keyword filter)]))

(def history
  (doto (History.)
    (events/listen EventType.NAVIGATE
                   (fn [event] (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
;; -------------------------

(defn mount-root []
  (reagent/render [clchess.views/clchess-app] (utils/by-id "app")))

(defn listen-to-events []
  (log/debug "listen-to-events")
  (.addEventListener js/window "resize"
                     #(dispatch [:view/resized]))
  (utils/fullscreen-change #(dispatch [:view/fullscreen-changed %])))

(defn reset-page []
  (log/merge-config! {:ns-whitelist ["clchess.core"
                                     #_"clchess.board"
                                     "clchess.events"
                                     "clchess.subs"
                                     #_"clchess.views"
                                     "clchess.events-common"
                                     "scid.*"
                                     "clchess.node_subs"
                                     #_"clchess.ctrl"]})
  (log/set-level! :debug)
  (dispatch-sync [:initialise-db])
  (dispatch-sync [:game/update-board])
  (let [theme (subscribe [:theme])]
    (theme/init-theme @theme))
  (mount-root)
  (listen-to-events))

(defn init! []
  (reset-page))
