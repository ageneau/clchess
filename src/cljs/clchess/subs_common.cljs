(ns clchess.subs_common
  (:require [re-frame.core :refer [reg-sub]]
            [taoensso.timbre :as log]))

;; -- Subscription handlers and registration  ---------------------------------

(reg-sub
 :theme
 (fn [db _]
   (log/debug "reg-sub :theme")
   (:theme db)))

(reg-sub
 :board
 (fn [db _]
   (log/debug "reg-sub :board")
   (:board db)))

(reg-sub
 :game/moves
 (fn [db _]
   (log/debug "reg-sub :game/moves: " (get-in db [:game :moves]))
   (get-in db [:game :moves])))

(reg-sub
 :game/promotion
 (fn [db _]
   (log/debug "reg-sub :game/promotion: " (get-in db [:board :promotion]))
   (get-in db [:board :promotion])))

(reg-sub
 :file-selector/changed
 (fn [db _]
   (log/debug "reg-sub :file-selector/changed ")
   (:file-selector db)))
