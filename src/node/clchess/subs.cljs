(ns clchess.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [taoensso.timbre :as log]
            [clchess.subs_common]
            [scid.game :as game]
            [scid.base :as base]))

;; -- Subscription handlers and registration  ---------------------------------
(register-sub
 :game/list
 (fn [db _]
   (log/debug "register-sub :game/list ")
   (reaction (when-let [db (get-in @db [:databases :current])]
               (log/debug ":game-list reaction:" db)
               (log/debug ":game-list reaction list:" (game/get-list (:key db)))
               (game/get-list (:key db))))))
