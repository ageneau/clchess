(ns clchess.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [node-webkit.core :as nw]
              [clojure.string :as string]
              [scid.core :as scid]))

(enable-console-print!)

(defn by-id [id]
  (.getElementById js/document (name id)))

(def chess (js/Chess.))
(def loaded-game (js/Chess.))
(def current-ply 0)

(def *chessground*)

(def SQUARES (.-SQUARES chess))

(defn color []
  (if (= (.turn chess) "w") "white" "black"))

;; {"a2":["a3","a4"],"b2":["b3","b4"],"c2":["c3","c4"],"d2":["d3","d4"],"e2":["e3","e4"],"f2":["f3","f4"],"g2":["g3","g4"],"h2":["h3","h4"],"b1":["a3","c3"],"g1":["f3","h3"]}
(defn moves [square]
  (let [dests
        (map (fn [move] ((js->clj move) "to"))
             (.moves chess #js {:square square :verbose true}))]
    (when (not-empty dests) {square dests})))

(defn dest-squares []
  (clj->js (reduce merge (map moves SQUARES))))

(defn on-move [orig dest]
  (println "on-move: " orig "->" dest)
  (.move chess #js {:from orig :to dest})
  (.set *chessground* #js {:turnColor (color) :movable #js {:color (color) :dests (dest-squares)}})
  (println "board fen:" (.getFen *chessground*)))

(defn reset-board []
  (println "reset-board")
  (.reset chess)
  (.set *chessground* #js {
                         :viewOnly false
                         :autoCastle true
                         :turnColor "white"
                         :fen "start"
                         :animation #js { :duration 500 }
                         :movable #js {
                                       :free false
                                       :color (color)
                                       :premove true
                                       :dests (dest-squares)
                                       :events #js { :after on-move }
                                       }
                         :drawable #js { :enabled true }
                         }))

(defn read-file [file]
  (print "read: " file)
  (let [fs (js/require "fs")]
    (when (.existsSync fs file)
      (.readFileSync fs file "utf8"))))

(defn long-str [& strings] (string/join "\n" strings))

(def PGN_TEST
  (long-str "[Event \"Casual Game\"]"
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

(defn load-pgn [pgn]
  (println "load-pgn: " pgn)
  (set! current-ply 0)
  (.reset chess)
  (.reset loaded-game)
  (.load_pgn loaded-game pgn)
  (println "FEN: " (.fen loaded-game))
  (.set *chessground* #js {
                         :viewOnly true
                         :autoCastle true
                         :turnColor "white"
                         :fen "start"
                         :animation #js { :duration 500 }
                         :drawable #js { :enabled true }
                         }))

;; -------------------------
;; Views

(defn reset-button []
  [:input {:type "button" :value "Reset"
           :on-click #(reset-board)}])

(def app-state
  (reagent/atom
   {:games
    (scid.core/game-list)}))

(defn update-games! [f & args]
  (apply swap! app-state update-in [:games] f args))

(defn display-game-info [{:keys [date result length wplayer bplayer welo belo move] :as game}]
  (str date ", " wplayer ", " bplayer))

(defn game [{:keys [date result length wplayer bplayer welo belo move] :as game}]
  [:tr
   [:td date]
   [:td result]
   [:td length]
   [:td wplayer]
   [:td welo]
   [:td bplayer]
   [:td belo]
   [:td move]])

(defn test-list []
  [:div {:id "test-list"} "TEST"])

(defn game-list []
  [:table {:id "game-list"}
   [:thead
    [:th {:class "date"} "Date"]
    [:th {:class "result"} "Result"]
    [:th {:class "length"} "Length"]
    [:th {:class "player-name"} "White"]
    [:th {:class "player-elo"} "W-Elo"]
    [:th {:class "player-name"} "Black"]
    [:th {:class "player-elo"} "B-Elo"]
    [:th {:class "move-list"} "Move"]]

   [:tbody
    (for [c (:games @app-state)]
      ^{:key (:id c)}
      [game c])]])

(defn atom-input [value]
  [:input {:field :file
           :type :file
           :accept ".pgn"
           :value @value
           :id "file-selector"
           :on-change (fn [val]
                        (let [file (-> val .-target .-value)]
                          (reset! value (-> val .-target .-value))
                          (load-pgn (read-file file))))}])

(defn shared-state []
  (let [val (reagent/atom "None")]
    (fn []
      [:div
       [:p "Selected file: " @val]
       [atom-input val]])))

(defn chessboard []
  [:div {:class "chessground normal wood cburnett" :id "chessground-container"}])

(defn controls []
  [:div {:id "controls"}
    [:input {:type "button" :value "back"}]
    [:input {:type "button" :value "next"
             :on-click #(let [hist (.history loaded-game #js {:verbose true})
                              move (js->clj (get hist current-ply))]
                          (println "From: " (move "from") " to:" (move "to"))
                          (if (= current-ply (dec (count hist)))
                            (js/alert "End of game")
                            (do
                              (.move *chessground* (move "from") (move "to"))
                              (set! current-ply (inc current-ply))))
                          )}]
    [shared-state]
    [reset-button]])

(defn home-page []
  [:div {:id "page-container"}
   [:div {:id "chessboard-and-controls"}
    [chessboard]
    [controls]]
   [:div {:id "game-list-container"} [game-list]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (nw/menubar! [{:label "File"
                 :submenu (nw/menu [{:label "Open"
                                     :click #(let [selector (.getElementById js/document "file-selector")]
                                               (.click selector))}
                                    {:label "Quit"
                                     :click nw/quit}])}])
  (mount-root)
  (set! *chessground* (js/Chessground. (by-id "chessground-container")))
  (reset-board))
