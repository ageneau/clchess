(ns ^:figwheel-no-load clchess.dev
  (:require [clchess.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [taoensso.timbre :as log]
            [re-frisk.core :refer [enable-re-frisk!]]
            [devtools.core :as devtools]))


(defn console-appender-devtools
  "Returns a simple js/console appender for ClojureScript.

  For accurate line numbers in Chrome, add these Blackbox[1] patterns:
    `/taoensso/timbre/appenders/core\\.js$`
    `/taoensso/timbre\\.js$`
    `/cljs/core\\.js$`

  [1] Ref. https://goo.gl/ZejSvR"

  ;; TODO Any way of using something like `Function.prototype.bind`
  ;; (Ref. https://goo.gl/IZzkQB) to get accurate line numbers in all
  ;; browsers w/o the need for Blackboxing?

  [& [opts]]
  {:enabled?   true
   :async?     false
   :min-level  nil
   :rate-limit nil
   :output-fn  :inherit
   :fn
   (if (exists? js/console)
     (let [;; Don't cache this; some libs dynamically replace js/console
           level->logger
           (fn [level]
             (or
               (case level
                 :trace  js/console.trace
                 :debug  js/console.debug
                 :info   js/console.info
                 :warn   js/console.warn
                 :error  js/console.error
                 :fatal  js/console.error
                 :report js/console.info)
               js/console.log))]

       (fn [data]
         (when-let [logger (level->logger (:level data))]
           (let [output
                 ((:output-fn data)
                  (assoc data
                         :msg_  ""
                         :?err nil))
                 ;; (<output> <raw-error> <raw-arg1> <raw-arg2> ...):
                 args (->> (:vargs data) ;; (cons (:?err data))
                           (cons output))]

             (.apply logger js/console (into-array args))))))

     (fn [data] nil))})

(defn setup-dev-environment
  []
  (log/debug "Setup dev enviroment")
  (log/set-config! {:ns-whitelist [#_"clchess.core"
                                   "clchess.board"
                                   "clchess.events"
                                   "clchess.subs"
                                   #_"clchess.theme"
                                   "clchess.dev"
                                   "clchess.db"
                                   #_"clchess.views"
                                   #_"clchess.widgets"
                                   "clchess.events-common"
                                   #_"clchess.board"
                                   #_"scid.*"
                                   #_"clchess.node_subs"
                                   "clchess.ctrl"]
                    :appenders
                    {:console-appender-devtools (console-appender-devtools)}})
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
