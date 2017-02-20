(ns clchess.specs.chessdb
  (:require [cljs.spec :as s]))

(s/def ::key string?)
(s/def ::name string?)
(s/def ::type #{:scid})
(s/def ::opened boolean)

(s/def ::database (s/keys :req [::key
                                ::name
                                ::type
                                ::opened]))

(s/def ::current ::database)
(s/def ::all (s/map-of string? ::database))

(s/def ::databases (s/keys :opt [::current
                                 ::all]))
