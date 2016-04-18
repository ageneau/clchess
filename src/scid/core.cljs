(ns scid.core)

(enable-console-print!)

(def scid (js/require "./build/Debug/scid"))
(def *db-file* "/home/BIG/src/CHESS/scid-code/Blitz")

(defn base-id []
  (or (.base scid "slot" *db-file*)
      (.base scid "open" *db-file*)))

(defn test-scid []
  (let [base-id (base-id)
        num-games (.base scid "numGames" base-id)]
    (println "BaseId: " base-id)
    (println "NumGames: " num-games)

    (.base scid "sortcache" base-id "create" "i-d-")
    (let [start 0
          count 11
          filter-name "dbfilter"
          sort-crit "i-d-"
          game-list (.base scid "gameslist" base-id start count filter-name sort-crit)]
      (println "Game list:" (js->clj game-list)))))

(defrecord GameInfo [id ; 723
                     result ; "1-0"
                     nmoves ;10
                     wplayer ;"Nemenyi9999"
                     welo ; "1931"
                     bplayer ;"erikelrojo"
                     belo  ;"1854"
                     date ;"2014.01.15"
                     event ;
                     server ;
                     pgn ;
                     ])

(defn game-list []
  (let [base-id (base-id)
        start 0
        count 11
        filter-name "dbfilter"
        sort-crit "i-d-"]
    (.base scid "sortcache" base-id "create" sort-crit)
    (map
     (fn [[id game & rest]]
       ;; (723 "1-0" 10 "Nemenyi9999" "1931" "erikelrojo" "1854" "2014.01.15" "Live Chess" "?" "Chess.com" 0 0 0 " " "" "" "QRRBBNN6:QRRBBN7" " " "????.??.??" 2014 1892 27 "1.e4 e5  2.Nf3 Nc6  3.c3 Nf6  4.d4 Nxe4  5.d5 Nb8")
       (let [[id ; 723
              result ; "1-0"
              nmoves ;10
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
              pgn ;
              :as game-info
              ] (array-seq game)]
         (map->GameInfo {:id id
                         :result result
                         :nmoves nmoves
                         :wplayer wplayer
                         :welo welo
                         :bplayer bplayer
                         :belo belo
                         :date date
                         :event event
                         :server server
                         :pgn pgn})))
     (->> (.base scid "gameslist" base-id start count filter-name sort-crit)
          array-seq
          (partition 3)))))
