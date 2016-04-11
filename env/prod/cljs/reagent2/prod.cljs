(ns reagent2.prod
  (:require [reagent2.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
