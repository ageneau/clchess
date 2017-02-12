(ns clchess.events
  (:require
   [re-frame.core :refer [dispatch reg-fx reg-event-db reg-event-fx path trim-v after debug]]
   [clchess.events-common]
   [clchess.utils :as utils]
   [taoensso.timbre :as log]))

(reg-fx
 :request-fullscreen
 (fn  [_]
   (utils/request-fullscreen (utils/by-id "app"))))

(reg-fx
 :exit-fullscreen
 (fn [_]
   (utils/exit-fullscreen)))

(reg-fx
 :fullscreen-change
 (fn [_]
   (utils/fullscreen-change #(dispatch [:view/fullscreen-changed %]))))

(reg-event-fx
 :view/toggle-fullscreen
 [(path :view)
  trim-v]
 (fn [cofx _]
   (let [is-fullscreen (:is-fullscreen (:db cofx))
         new-state (not is-fullscreen)]
     (log/debug "Toggle full screen: " is-fullscreen ", " (utils/get-viewport-size))
     (if new-state
       {:request-fullscreen
        :fullscreen-change}
       {:exit-fullscreen
        :fullscreen-change}))))

(reg-event-db
 :view/fullscreen-changed
 [(path :view)
  trim-v]
 (fn [{is-fullscreen :is-fullscreen :as old} [new-value]]
   (log/debug "Full screen changed: " is-fullscreen ", " new-value)
   (assoc old :is-fullscreen new-value)))
