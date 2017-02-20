(ns clchess.specs.board
  (:require [cljs.spec :as s]
            [cljsjs.chess.js]))


(def ^:const squares
  (into #{} (let [chess (js/Chess.)]
              (js->clj (.-SQUARES chess)))))

(s/def ::square squares)
(s/def ::move (s/coll-of ::square :kind vector? :count 2 :distinct true))

(s/def ::square-kw (into #{} (map keyword squares)))

(s/def ::piece #{"q" "k" "b" "r" "n" "p"})

(s/def ::turn #{"white" "black"})
(s/def ::dests (s/map-of ::square (s/coll-of ::square)))

(s/def ::board any?)
