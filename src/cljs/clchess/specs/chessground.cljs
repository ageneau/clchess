(ns clchess.specs.chessground
  (:require [cljs.spec :as s]
            [clchess.specs.chess :as schess]
            [clchess.specs.board :as sboard]))

(s/def ::viewOnly boolean?)
(s/def ::turnColor ::sboard/turn)
(s/def ::lastMove (s/nilable ::sboard/move))
(s/def ::color #{"white" "black" "both"})
(s/def ::fen ::schess/fen)
(s/def ::free boolean?)
(s/def ::dests ::sboard/dests)

(s/def ::movable (s/keys :req [::free
                               ::color
                               ::premove
                               ::dests]))
(s/def ::show boolean?)
(s/def ::from ::sboard/square)
(s/def ::to ::sboard/square)
(s/def ::player ::sboard/turn)


(s/def ::promotion (s/keys :req [::show]
                           :opt [::from
                                 ::to
                                 ::player]))

(s/def ::board (s/keys :req [::viewOnly
                             ::turnColor
                             ::lastMove
                             ::fen
                             ::promotion
                             ::movable]))

