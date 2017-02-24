(ns clchess.specs.board
  (:require #?(:clj [clojure.spec :as s]
               :cljs [cljs.spec :as s])
            [clchess.data.board :as dboard]))

(s/def ::square dboard/squares)
(s/def ::move (s/coll-of ::square :kind vector? :count 2 :distinct true))

(s/def ::square-kw (into #{} (map keyword dboard/squares)))

(s/def ::piece #{"q" "k" "b" "r" "n" "p"})

(s/def ::turn #{"white" "black"})
(s/def ::dests (s/map-of ::square (s/coll-of ::square)))

(s/def ::board any?)
