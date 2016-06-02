(ns clchess.test
    (:require [clchess.utils :as utils]))

(def game-list
  '({:date "2002.02.26", :server "jeu.echecs.com", :move "1.Nf3 Nf6  2.d4 d5  3.e3 Bf5  4.Bb5+ c6  5.Bd3 Bxd3", :bplayer "vasyli", :event "ICS rated blitz match", :result "=-=", :id 44, :length 67, :wplayer "Sjeng", :belo "1871", :welo "2494"}
    {:date "2002.02.27", :server "jeu.echecs.com", :move "1.d4 Nf6  2.c4 e6  3.Nc3 d5  4.cxd5 exd5  5.Bg5 Be7", :bplayer "Amyan", :event "ICS rated blitz match", :result "1-0", :id 53, :length 63, :wplayer "vasyli", :belo "2361", :welo "1883"}
    {:date "2002.02.09", :server "jeu.echecs.com", :move "1.d4 d5  2.c4 e6  3.Nc3 Nf6  4.cxd5 exd5  5.Bg5 c6", :bplayer "ZChess", :event "ICS rated blitz match", :result "1-0", :id 57, :length 25, :wplayer "vasyli", :belo "2316", :welo "1924"}
    {:date "2005.01.10", :server "freechess.org", :move "1.d4 Nf6  2.c4 g6  3.Nc3 Bg7  4.e4 d6  5.Nf3 O-O", :bplayer "tentacle", :event "ICS rated standard match", :result "1-0", :id 110, :length 55, :wplayer "xgold", :belo "2110", :welo "1954"}
    {:date "2013.11.14", :server "Chess.com", :move "1.d4 Nf6  2.c4 e6  3.Nc3 d5  4.cxd5 exd5  5.Bg5 Nbd7", :bplayer "andelser", :event "Trofeo Iberoamericano - Board 5", :result "1-0", :id 315, :length 36, :wplayer "erikelrojo", :belo "2030", :welo "1967"}
    {:date "2013.11.14", :server "Chess.com", :move "1.Nf3 d5  2.g3 Nf6  3.Bg2 c5  4.O-O Nc6  5.d4 e6", :bplayer "erikelrojo", :event "Trofeo Iberoamericano - Board 5", :result "0-1", :id 316, :length 46, :wplayer "andelser", :belo "1984", :welo "1981"}
    {:date "2004.12.10", :server "freechess.org", :move "1.d4 Nf6  2.c4 g6  3.Nc3 Bg7  4.e4 d6  5.Nf3 O-O", :bplayer "dexivoje", :event "ICS rated standard match", :result "1-0", :id 67, :length 54, :wplayer "xgold", :belo "2015", :welo "1928"}
    {:date "2014.01.15", :server "Chess.com", :move "1.e4 e5  2.Nf3 Nc6  3.c3 Nf6  4.d4 Nxe4  5.d5 Nb8", :bplayer "erikelrojo", :event "Live Chess", :result "1-0", :id 723, :length 10, :wplayer "Nemenyi9999", :belo "1854", :welo "1931"}
    {:date "2014.01.15", :server "Chess.com", :move "1.d4 d5  2.c4 dxc4  3.Nf3 Nf6  4.e3 e6  5.Bxc4 c5", :bplayer "Nemenyi9999", :event "Live Chess", :result "0-1", :id 724, :length 20, :wplayer "erikelrojo", :belo "1937", :welo "1848"}
    {:date "2014.01.10", :server "Chess.com", :move "1.e4 e5  2.d4 exd4  3.c3 dxc3  4.Bc4 cxb2  5.Bxb2 d6", :bplayer "erikelrojo", :event "Live Chess", :result "1-0", :id 772, :length 18, :wplayer "totosteel", :belo "1827", :welo "1965"}
    {:date "2005.01.22", :server "freechess.org", :move "1.e4 e6  2.d3 c5  3.g3 Nc6  4.Bg2 g6  5.Ne2 Bg7", :bplayer "xgold", :event "ICS rated blitz match", :result "=-=", :id 21, :length 60, :wplayer "Erp", :belo "1737", :welo "2098"}))


(def PGN_TEST
  (utils/long-str "[Event \"Casual Game\"]"
            "[Site \"Berlin GER\"]"
            "[Date \"1852.??.??\"]"
            "[EventDate \"?\"]"
            "[Round \"?\"]"
            "[Result \"1-0\"]"
            "[White \"Adolf Anderssen\"]"
            "[Black \"Jean Dufresne\"]"
            "[ECO \"C52\"]"
            "[WhiteElo \"?\"]"
            "[BlackElo \"?\"]"
            "[PlyCount \"47\"]"
            ""
            "1.e4 e5 2.Nf3 Nc6 3.Bc4 Bc5 4.b4 Bxb4 5.c3 Ba5 6.d4 exd4 7.O-O"
            "d3 8.Qb3 Qf6 9.e5 Qg6 10.Re1 Nge7 11.Ba3 b5 12.Qxb5 Rb8 13.Qa4"
            "Bb6 14.Nbd2 Bb7 15.Ne4 Qf5 16.Bxd3 Qh5 17.Nf6+ gxf6 18.exf6"
            "Rg8 19.Rad1 Qxf3 20.Rxe7+ Nxe7 21.Qxd7+ Kxd7 22.Bf5+ Ke8"
            "23.Bd7+ Kf8 24.Bxe7# 1-0"))
