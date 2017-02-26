(ns clchess.specs.chess
  (:require #?(:clj [clojure.spec :as s]
               :cljs [cljs.spec :as s])
            #?(:cljs [cljsjs.chess.js])
            [clchess.specs.board :as sboard]))

(s/def ::fen string?)
(s/def ::pgn string?)

(s/def ::san string?)
(s/def ::color #{"w" "b"})
(s/def ::from ::sboard/square)
(s/def ::to ::sboard/square)
(s/def ::flags string?)
(s/def ::piece ::sboard/piece)
(s/def ::captured ::piece)
(s/def ::promotion ::sboard/piece)

(s/def ::move (s/keys :req [::color
                            ::from
                            ::to
                            ::flags
                            ::piece
                            ::san]
                      :opt [::fen
                            ::captured
                            ::promotion]))

(s/def ::last-move ::move)

(s/def ::initial-fen ::fen)
(s/def ::ply integer?)
(s/def ::current-ply ::ply)
(s/def ::moves (s/coll-of ::move :kind vector?))

(s/def ::game (s/keys :req [::initial-fen
                            ::moves
                            ::current-ply]))
