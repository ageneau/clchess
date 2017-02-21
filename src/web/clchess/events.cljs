(ns clchess.events
  (:require
   [re-frame.core :refer [dispatch reg-fx reg-event-db reg-event-fx path trim-v after debug]]
   [clchess.events-common :refer [view-interceptor generic-interceptor]]
   [clchess.utils :as utils]
   [taoensso.timbre :as log]))

(reg-fx
 :view/request-fullscreen
 (fn  [_]
   (utils/request-fullscreen (utils/by-id "app"))))

(reg-fx
 :view/exit-fullscreen
 (fn [_]
   (utils/exit-fullscreen)))

(reg-event-fx
 :view/toggle-fullscreen
 view-interceptor
 (fn [cofx _]
   (let [is-fullscreen (:is-fullscreen (:db cofx))
         new-state (not is-fullscreen)]
     (log/debug "Toggle full screen: " is-fullscreen ", " (utils/get-viewport-size))
     (if new-state
       {:view/request-fullscreen nil}
       {:view/exit-fullscreen nil}))))

(reg-event-db
 :view/fullscreen-changed
 view-interceptor
 (fn [{is-fullscreen :is-fullscreen :as db} [new-value]]
   (log/debug "Full screen changed: " is-fullscreen ", " new-value)
   (assoc db :is-fullscreen new-value)))
