(ns clchess.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [secretary.core :as secretary]

            [goog.events :as events]
            ;; [node-webkit.core :as nw]
            [clchess.utils :as utils]
            [clchess.board :as board]
            [clchess.handlers] ;; Do not remove this. This is needed for handler registration
            [clchess.subs] ;; Do not remove this. This is needed for subscription handler registration
            [clchess.views]
            [clchess.theme :as theme]
            [taoensso.timbre :as log])
  (:import [goog History]
           [goog.history EventType]))

(enable-console-print!)

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

(defn reset-page []
  (log/merge-config! {:ns-whitelist ["clchess.board"
                                     "clchess.handlers"
                                     "clchess.ctrl"]})
  (log/set-level! :debug)
  (dispatch-sync [:initialise-db])
  (dispatch-sync [:game/update-board])
  (let [theme (subscribe [:theme])]
    (theme/init-theme @theme))
  (mount-root))

(defn init! []
  ;; (nw/menubar! [{:label "File"
  ;;                :submenu (nw/menu [{:label "Open"
  ;;                                    :click #(let [selector (utils/by-id "file-selector")]
  ;;                                              (.click selector))}
  ;;                                   {:label "Quit"
  ;;                                    :click nw/quit}])}])
  (reset-page))
