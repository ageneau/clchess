(ns clchess.subs
  (:require
   [re-frame.core :refer [reg-sub]]
   [clchess.subs_common]
   [clchess.test :as test]
   [taoensso.timbre :as log]))

(reg-sub
 :game/list
 (fn [db _]
   (log/debug "reg-sub :game/list ")
   test/game-list))
