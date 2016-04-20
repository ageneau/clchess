(ns clchess.utils
    (:require [clojure.string :as string]))

(defn read-file [file]
  (print "read: " file)
  (let [fs (js/require "fs")]
    (when (.existsSync fs file)
      (.readFileSync fs file "utf8"))))

(defn long-str [& strings] (string/join "\n" strings))

(defn by-id [id]
  (.getElementById js/document (name id)))
