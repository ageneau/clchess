(ns clchess.subs
  (:require
   [re-frame.core :refer [reg-sub reg-sub-raw dispatch]]
   [reagent.ratom :as reagent :refer [make-reaction]]
   [clchess.subs_common]
   [clchess.test :as test]
   [clchess.utils :as utils]
   [taoensso.timbre :as log]))

(reg-sub
 :game/list
 (fn [db _]
   (log/debug "reg-sub :game/list ")
   test/game-list))
