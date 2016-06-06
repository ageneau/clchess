(ns clchess.handlers
  (:require
   [re-frame.core :refer [dispatch register-handler path trim-v after debug]]
   [clchess.handlers-common]
   [clchess.utils :as utils]
   [taoensso.timbre :as log]))

(register-handler
 :menu/full-screen
 [(path :view)
  trim-v]
 (fn [{is-full-screen :is-full-screen :as old} _]
   (log/debug "Toggle full screen: " is-full-screen ", " (utils/get-viewport-size))
   (let [new-state (not is-full-screen)]
     (if new-state
       (utils/request-full-screen (utils/by-id "app"))
       (utils/exit-full-screen))
     (utils/full-screen-change #(dispatch [:view/full-screen-changed %]))
     old)))

(register-handler
 :view/full-screen-changed
 [(path :view)
  trim-v]
 (fn [{is-full-screen :is-full-screen :as old} [new-value]]
   (log/debug "Full screen changed: " is-full-screen ", " new-value)
   (assoc old :is-full-screen new-value)))
