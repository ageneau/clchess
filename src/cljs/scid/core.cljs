(ns scid.core
  (:require [taoensso.timbre :as log]))

(def ^:private scid (js/require "./build/Debug/scid"))
#_(def *db-file* "/home/BIG/src/CHESS/scid-code/Blitz")

