(ns clchess.subs
  (:require [re-frame.core :refer [reg-sub]]
            [taoensso.timbre :as log]
            [clchess.subs_common]
            [clchess.specs.chessdb :as schessdb]
            [scid.game :as game]
            [scid.base :as base]))

;; -- Subscription handlers and registration  ---------------------------------
(reg-sub
 :game/list
 (fn [db _]
   (when-let [db (get-in db [::schessdb/databases ::schessdb/current])]
     (game/get-list (::schessdb/key db)))))
