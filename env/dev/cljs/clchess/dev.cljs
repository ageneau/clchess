(ns ^:figwheel-no-load clchess.dev
  (:require [clchess.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [taoensso.timbre :as timbre]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback #(do
                      (timbre/info "Figwheel reload")
                      (core/reset-page)))

(core/init!)
