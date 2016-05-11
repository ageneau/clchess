(ns uci.core
  (:require-macros
   [uci.macros :refer [inspect breakpoint]]
   [cljs.core.async.macros :refer [go]])
  (:require [cljs-promises.core :as p]
            [cljs-promises.async :refer [pair-port] :refer-macros [<?]]
            [cljs.core.async :refer [put! chan <!]]
            [uci.utils :as utils]))

(def uci (js/require "uci"))
(def *engine* (new uci "/usr/games/stockfish"))

;; (cljs-promises.async/extend-promises-as-pair-channels!)

(defn run-engine []
  (let [engine *engine*
        promise (.runProcess engine)]
    (try
      (-> promise
          (p/then (fn []
                    (println "Started")
                    (.uciCommand engine)))
          (p/then (fn [id-and-opts]
                    (println "Opts" id-and-opts)
                    (.isReadyCommand engine)))
          (p/then (fn []
                    (println "Ready")
                    (.uciNewGameCommand engine)))
          (p/then (fn []
                    (println "New game started")
                    (.positionCommand engine "startpos" "e2e4 e7e5")))
          (p/then (fn []
                    (println "Starting positition set")
                    (println "Starting analysis")
                    (.goInfiniteCommand engine
                                        (fn [info]
                                          (println info)))))
          (utils/pdelay 4000)
          (p/then (fn []
                    (let [stop (.stopCommand engine)]
                      (println "Stopping analysis: " stop)
                      (inspect stop)
                      stop)))
          (p/then (fn [bestmove]
                    (println "Best move:" (utils/jsx->clj bestmove))
                    (inspect bestmove)
                    (.quitCommand engine)))
          (p/then (fn []
                    (println "Stopped"))))
      (catch js/Error e
        (println (str "Error when running UCI engine: " (ex-message e)))))))
