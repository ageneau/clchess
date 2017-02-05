(ns clchess.handlers
  (:require
   [clchess.handlers-common]
   [clojure.string :as string]
   [re-frame.core :refer [dispatch reg-event-db path trim-v after debug]]
   [node-webkit.core :as nw]
   [clchess.utils :as utils]
   [clchess.ctrl :as ctrl]
   [scid.base :as chessdb]
   [taoensso.timbre :as log]))

(reg-event-db
 :db/open
 [(path :databases)
  trim-v]
 (fn [databases [fn]]
   (log/debug "Databases: " databases ", " fn ", " (find fn databases))
   (if-not (find fn databases)
     (let [key (chessdb/open (utils/remove-extension fn))
           new {:key key :name fn :type :scid :opened true}]
       (log/debug "key=" key ", " new)
       (-> databases
           (assoc :current new)
           (assoc-in [:all key] new)))
     databases)))


(reg-event-db
 :menu/full-screen
 [(path :view)
  trim-v]
 (fn [{is-full-screen :is-full-screen :as old} _]
   (log/debug "Toggle full screen: " is-full-screen)
   (let [window (nw/window)
         new-state (not is-full-screen)]
     (if new-state
       (.enterFullscreen window)
       (.leaveFullscreen window))
     (assoc old :is-full-screen new-state))))

(reg-event-db
 :menu/quit
 [trim-v]
 (fn [old _]
   (log/debug "Quit")
   (nw/quit)
   old))
