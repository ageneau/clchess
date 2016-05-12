(ns clchess.utils
  (:require [clojure.string :as string]
            [goog.dom :as dom]
            [goog.dom.classlist :as classlist]
            [goog.events :as events]
            [cljs.core.async :as async :refer [put! chan <!]]
            [taoensso.timbre :as log]))

(defn read-file [file]
  (print "read: " file)
  (let [fs (js/require "fs")]
    (when (.existsSync fs file)
      (.readFileSync fs file "utf8"))))

(defn long-str [& strings] (string/join "\n" strings))

(defn by-id [id]
  (.getElementById js/document (name id)))

(defn body []
  (.-body js/document))

;; From cljs-promises/examples/src/examples/replicated_search.cljs
(defn listen [el type]
  (let [c (chan)]
    (events/listen el type #(put! c %))
    c))

;; https://github.com/reagent-project/reagent/wiki/Beware-Event-Handlers-Returning-False
(defmacro handler-fn
  ([& body]
   `(fn [~'event] ~@body nil)))  ; always return nil
