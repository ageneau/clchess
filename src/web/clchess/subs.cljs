(ns clchess.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [re-frame.core :refer [register-sub]]
   [clchess.subs_common]
   [clchess.test :as test]
   [taoensso.timbre :as log]))

(register-sub
 :game/list
 (fn [db _]
   (log/debug "register-sub :game/list ")
   (reaction test/game-list)))
