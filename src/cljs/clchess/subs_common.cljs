(ns clchess.subs_common
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [taoensso.timbre :as log]))

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
