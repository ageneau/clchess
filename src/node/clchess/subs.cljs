(ns clchess.subs
  (:require [re-frame.core :refer [reg-sub]]
            [taoensso.timbre :as log]
            [clchess.subs_common]
            [scid.game :as game]
            [scid.base :as base]))

;; -- Subscription handlers and registration  ---------------------------------
(reg-sub
 :game/list
 (fn [db _]
   (log/debug "reg-sub :game/list ")
   (when-let [db (get-in @db [:databases :current])]
     (log/debug ":game-list:" db)
     (log/debug ":game-list list:" (game/get-list (:key db)))
     (game/get-list (:key db)))))
