(ns clchess.subs_common
  (:require [re-frame.core :refer [reg-sub]]
            [clchess.specs.board :as sboard]
            [clchess.specs.theme :as stheme]
            [clchess.specs.chess :as schess]
            [taoensso.timbre :as log]))

;; -- Subscription handlers and registration  ---------------------------------

(reg-sub
 :theme
 (fn [db _]
   (log/debug "reg-sub :theme")
   (::stheme/theme db)))

(reg-sub
 :board
 (fn [db _]
   (log/debug "reg-sub :board")
   (::sboard/board db)))

(reg-sub
 :game/moves
 (fn [db _]
   (log/debug "reg-sub :game/moves: " (get-in db [::schess/game ::sboard/moves]))
   (get-in db [::schess/game ::schess/moves])))

(reg-sub
 :game/promotion
 (fn [db _]
   (log/debug "reg-sub :game/promotion: " (get-in db [::sboard/board :promotion]))
   (get-in db [::sboard/board :promotion])))

(reg-sub
 :file-selector/changed
 (fn [db _]
   (log/debug "reg-sub :file-selector/changed ")
   (:file-selector db)))
