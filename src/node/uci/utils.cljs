(ns uci.utils
  (:require [cljs-promises.core :as p]))

(defn pdelay [promise ms]
  (p/then (p/timeout ms)
          (fn []
            promise)))

(defn jsx->clj
  [x]
  (into {} (for [k (.keys js/Object x)] [k (aget x k)])))
