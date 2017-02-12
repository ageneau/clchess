(ns clchess.events
  (:require
   [clchess.events-common]
   [clojure.string :as string]
   [re-frame.core :refer [dispatch reg-event-db reg-fx reg-event-fx path trim-v after debug]]
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


(reg-fx
 :request-fullscreen
 (fn  [_]
   (.enterFullscreen (nw/window))))

(reg-fx
 :exit-fullscreen
 (fn [_]
   (.leaveFullscreen (nw/window))))

(reg-event-fx
 :view/toggle-fullscreen
 [(path :view)
  trim-v]
 (fn [cofx _]
   (let [is-fullscreen (:is-fullscreen (:db cofx))
         window (nw/window)
         new-state (not is-fullscreen)]
     (log/debug "Toggle full screen: " is-fullscreen)
     (if new-state
       {:db (assoc (:db cofx) :is-fullscreen new-state)
        :request-fullscreen []}
       {:db (assoc (:db cofx) :is-fullscreen new-state)
        :exit-fullscreen []}))))

(reg-event-db
 :menu/quit
 [trim-v]
 (fn [old _]
   (log/debug "Quit")
   (nw/quit)
   old))
