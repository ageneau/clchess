(ns scid.base
  (:require [taoensso.timbre :as log]))

(def ^:private scid (js/require "scid"))
#_(def *db-file* "/home/BIG/src/CHESS/scid-code/Blitz")

(defn ^:private slot [fn]
  (.base scid "slot" fn))

(defn opened? [fn]
  ((complement zero?) (slot fn)))

(defn open [fn]
  (log/debug "open:" fn "," (opened? fn))
  (let [key (if (opened? fn)
              (str (slot fn))
              (str (.base scid "open" fn)))]
    (log/debug "key:" key)
    key))

(defn ^:private id [fn]
  (or (let [slot (slot fn)]
        (when ((complement zero?) slot) slot))
      (open fn)))

