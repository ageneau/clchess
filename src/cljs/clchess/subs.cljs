(ns clchess.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [taoensso.timbre :as log]
            [scid.game :as game]
            [scid.base :as base]))

;; -- Subscription handlers and registration  ---------------------------------

(register-sub
 :theme
 (fn [db _]
   (log/debug "register-sub :theme")
   (reaction (:theme @db))))

(register-sub
 :board
 (fn [db _]
   (log/debug "register-sub :board")
   (reaction (:board @db))))

(register-sub
 :game/moves
 (fn [db _]
   (log/debug "register-sub :game/moves: " (get-in @db [:game :moves]))
   (reaction (get-in @db [:game :moves]))))

(register-sub
 :game/promotion
 (fn [db _]
   (log/debug "register-sub :game/promotion: " (get-in @db [:board :promotion]))
   (reaction (get-in @db [:board :promotion]))))

(register-sub
 :file-selector/changed
 (fn [db _]
   (log/debug "register-sub :file-selector/changed ")
   (reaction (:file-selector @db))))

(register-sub
 :game/list
 (fn [db _]
   (log/debug "register-sub :game/list ")
   (reaction (when-let [db (get-in @db [:databases :current])]
               (log/debug ":game-list reaction:" db)
               (game/get-list (:key db))))))
