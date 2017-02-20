(ns ^:figwheel-no-load clchess.dev
  (:require [clchess.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [taoensso.timbre :as log]
            [re-frisk.core :refer [enable-re-frisk!]]
            [devtools.core :as devtools]))

(defn setup-dev-environment
  []
  (log/debug "Setup dev enviroment")
  (log/merge-config! {:ns-whitelist ["clchess.core"
                                     #_"clchess.board"
                                     "clchess.events"
                                     "clchess.subs"
                                     "clchess.theme"
                                     "clchess.db"
                                     #_"clchess.views"
                                     "clchess.events-common"
                                     "scid.*"
                                     "clchess.node_subs"
                                     #_"clchess.ctrl"]})
  (log/set-level! :debug)
  (figwheel/watch-and-reload
   :websocket-url "ws://localhost:3449/figwheel-ws"
   :jsload-callback #(do
                       (log/info "Figwheel reload")
                       (core/reset-page)))
  (devtools/install!))

(set! core/pre-render-hook (conj core/pre-render-hook #(enable-re-frisk!)))
(set! core/page-load-hook (conj core/page-load-hook setup-dev-environment))

(core/init!)
