(ns scid.game
  (:require [scid.core :as core]
            [scid.base :as base]
            [taoensso.timbre :as log]))

(defn number []
  (.game base/scid "number"))

(defn pgn []
  (.game base/scid "pgn"
         "-symbols" "1"
         "-indentVar" "1"
         "-indentCom" "1"
         "-space" "0"
         "-format" "color"
         "-column" "0"
         "-short" "1"
         "-markCodes" "0"))

(defn tag []
  (.game base/scid "tag" "get" "Extra"))

(defn load [number]
  (.game base/scid "load" number))

(defn get-list [base-key]
  (let [base-id (int base-key)
        start 0
        count 11
        filter-name "dbfilter"
        sort-crit "i-d-"]
    (log/debug "get-list:" base-id)
    (.base base/scid "sortcache" base-id "create" sort-crit)
    (map
     (fn [[id game & rest]]
       ;; (723 "1-0" 10 "Nemenyi9999" "1931" "erikelrojo" "1854" "2014.01.15" "Live Chess" "?" "Chess.com" 0 0 0 " " "" "" "QRRBBNN6:QRRBBN7" " " "????.??.??" 2014 1892 27 "1.e4 e5  2.Nf3 Nc6  3.c3 Nf6  4.d4 Nxe4  5.d5 Nb8")
       (let [[id ; 723
              result ; "1-0"
              length ;10
              wplayer ;"Nemenyi9999"
              welo ; "1931"
              bplayer ;"erikelrojo"
              belo  ;"1854"
              date ;"2014.01.15"
              event ;
              _ ;
              server ;
              _ ;
              _ ;
              _ ;
              _ ;
              _ ;
              _ ;
              _ ;
              _ ;
              _ ;
              _ ;
              _ ;
              _ ;
              move ;
              :as game-info
              ] (array-seq game)]
         {:id id
          :result result
          :length length
          :wplayer wplayer
          :welo welo
          :bplayer bplayer
          :belo belo
          :date date
          :event event
          :server server
          :move move}))
     (->> (.base base/scid "gameslist" base-id start count filter-name sort-crit)
          array-seq
          (partition 3)))))
