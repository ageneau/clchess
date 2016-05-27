(ns clchess.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [taoensso.timbre :as log]))

;; -- Subscription handlers and registration  ---------------------------------

(register-sub
 :theme
 (fn [db _]
   (log/info "register-sub :theme")
   (reaction (:theme @db))))

(register-sub
 :board
 (fn [db _]
   (log/info "register-sub :board")
   (reaction (:board @db))))

(register-sub
 :game/moves
 (fn [db _]
   (log/info "register-sub :game/moves: " (get-in @db [:game :moves]))
   (reaction (get-in @db [:game :moves]))))
