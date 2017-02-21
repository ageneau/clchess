(ns clchess.events
  (:require
   [clchess.events-common :refer [view-interceptor databases-interceptor generic-interceptor]]
   [clojure.string :as string]
   [re-frame.core :refer [dispatch reg-event-db reg-fx reg-event-fx path trim-v after debug]]
   [node-webkit.core :as nw]
   [clchess.utils :as utils]
   [clchess.ctrl :as ctrl]
   [scid.base :as chessdb]
   [taoensso.timbre :as log]))

(reg-event-db
 :db/open
 databases-interceptor
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
 :view/request-fullscreen
 (fn  [_]
   (.enterFullscreen (nw/window))))

(reg-fx
 :view/exit-fullscreen
 (fn [_]
   (.leaveFullscreen (nw/window))))

(reg-fx
 :app/exit
 (fn [_]
   (nw/quit)))

(reg-event-fx
 :view/toggle-fullscreen
 view-interceptor
 (fn [cofx _]
   (log/debug "toggle-fullscreen")
   (let [is-fullscreen (:is-fullscreen (:db cofx))
         window (nw/window)
         new-state (not is-fullscreen)]
     (log/debug "Toggle full screen: " is-fullscreen)
     (if new-state
       {:db (assoc (:db cofx) :is-fullscreen new-state)
        :view/request-fullscreen nil}
       {:db (assoc (:db cofx) :is-fullscreen new-state)
        :view/exit-fullscreen nil}))))

(reg-event-fx
 :menu/quit
 generic-interceptor
 (fn [cofx _]
   (log/debug "Quit")
   {:app/exit nil}))
