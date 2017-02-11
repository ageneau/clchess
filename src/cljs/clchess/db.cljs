(ns clchess.db
  (:require [cljs.reader]
            [clchess.theme :as theme]
            [clchess.ctrl :as ctrl]
            [cljs.spec :as spec]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]))


;; -- Spec --------------------------------------------------------------------
;;
;; This is a clojure.spec specification for the value in app-db. It is like a
;; Schema. See: http://clojure.org/guides/spec
;;
;; The value in app-db should always match this spec. Only event handlers
;; can change the value in app-db so, after each event handler
;; has run, we re-check app-db for correctness (compliance with the Schema).
;;
;; How is this done? Look in events.cljs and you'll notice that all handers
;; have an "after" interceptor which does the spec re-check.
;;
;; None of this is strictly necessary. It could be omitted. But we find it
;; good practice.

(spec/def :theme/is-2d boolean?)
(spec/def :theme/theme theme/theme-names)
(spec/def :theme/data-theme theme/data-themes)
(spec/def :theme/data-theme-3d theme/data-themes-3d)
(spec/def :theme/data-set theme/data-sets)
(spec/def :theme/data-set-3d theme/data-sets-3d)
(spec/def :theme/background-img (spec/nilable string?))
(spec/def :theme/zoom string?)


(spec/def ::theme (spec/keys :req-un [:theme/is-2d
                                      :theme/theme
                                      :theme/data-theme
                                      :theme/data-theme-3d
                                      :theme/data-set
                                      :theme/data-set-3d
                                      :theme/background-img
                                      :theme/zoom]
                             ))

(spec/def :board/square ctrl/squares)
(spec/def :board/move (spec/coll-of :board/square :kind vector? :count 2 :distinct true))

(spec/def :board/square-kw (into #{} (map keyword ctrl/squares)))

(spec/def :board/piece #{"q" "k" "b" "r"})

(spec/def :board/turn #{"white" "black"})
(spec/def :board/dests (spec/map-of :board/square (spec/coll-of :board/square)))
(spec/def :chess/fen string?)

(spec/def :chessground/viewOnly boolean?)
(spec/def :chessground/turnColor :board/turn)
(spec/def :chessground/lastMove (spec/nilable :board/move))
(spec/def :chessground/color #{"white" "black" "both"})
(spec/def :chessground/fen :chess/fen)
(spec/def :chessground/free boolean?)
(spec/def :chessground/dests :board/dests)

(spec/def :chessground/movable (spec/keys :req-un [:chessground/free
                                                   :chessground/color
                                                   :chessground/premove
                                                   :chessground/dests]))
(spec/def :chessground/show boolean?)
(spec/def :chessground/from :board/square)
(spec/def :chessground/to :board/square)
(spec/def :chessground/player :board/turn)


(spec/def :chessground/promotion (spec/keys :req-un [:chessground/show]
                                            :opt-un [:chessground/from
                                                     :chessground/to
                                                     :chessground/player]))

(spec/def :chessground/board (spec/keys :req-un [:chessground/viewOnly
                                                 :chessground/turnColor
                                                 :chessground/lastMove
                                                 :chessground/fen
                                                 :chessground/promotion
                                                 :chessground/movable]))

(spec/def :chess/san string?)
(spec/def :chess/color #{"w" "b"})
(spec/def :chess/from :board/square)
(spec/def :chess/to :board/square)
(spec/def :chess/flags string?)
(spec/def :chess/piece :board/piece)
(spec/def :chess/promotion :board/piece)

(spec/def :chess/move (spec/keys :req-un [:chess/color
                                          :chess/from
                                          :chess/to
                                          :chess/flags
                                          :chess/piece
                                          :chess/promotion
                                          :chess/san
                                          :chess/fen]))

(spec/def :chessdb/key string?)
(spec/def :chessdb/name string?)
(spec/def :chessdb/type #{:scid})
(spec/def :chessdb/opened boolean)

(spec/def :chessdb/database (spec/keys :req-un [:chessdb/key
                                                :chessdb/name
                                                :chessdb/type
                                                :chessdb/opened]))

(spec/def :chess/initial-fen :chess/fen)
(spec/def :chess/ply integer?)
(spec/def :chess/current-ply :chess/ply)
(spec/def :chess/moves (spec/coll-of :chess/move :kind vector?))

(spec/def :chess/game (spec/keys :req-un [:chess/initial-fen
                                          :chess/moves
                                          :chess/current-ply]))

(spec/def ::is-full-screen boolean?)

(spec/def ::view (spec/keys :req-un [::is-full-screen]))

(spec/def :chessdb/current :chessdb/database)
(spec/def :chessdb/all (spec/map-of string? :chessdb/database))

(spec/def :chessdb/databases (spec/keys :req-opt [:chessdb/current
                                                  :chessdb/all]))

(spec/def :file-selector/opened boolean?)
(spec/def :file-selector/action #{:load-pgn :open-db})
(spec/def :file-selector/accept string?)

(spec/def ::file-selector (spec/keys :req-un [:file-selector/opened]
                                     :opt-un [:file-selector/action
                                              :file-selector/accept]))

(spec/def ::db (spec/keys :req-un [::theme
                                   ::view
                                   :board/board
                                   :chess/game
                                   :chessdb/databases
                                   ::file-selector]))

;; -- Default app-db Value  ---------------------------------------------------
;;
;; When the application first starts, this will be the value put in app-db
;; Unless, or course, there are todos in the LocalStore (see further below)
;; Look in core.cljs for  "(dispatch-sync [:initialise-db])"
;;

(def default-value            ;; what gets put into app-db by default.
  {:theme {:is-2d true
           :theme "light"
           :data-theme "blue"
           :data-theme-3d "Black-White-Aluminium"
           :data-set "cburnett"
           :data-set-3d "Basic"
           :background-img "http://lichess1.org/assets/images/background/landscape.jpg"
           :zoom "80%"}
   :view {:is-full-screen false}
   :board {:viewOnly false
           :turnColor "white"
           :lastMove nil
           :fen "start"
           :movable {:free false
                     :color "both"
                     :premove true
                     :dests {}}
           :promotion {:show false}}
   :game {:initial-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          :moves []
          :current-ply 0}
   :databases {}
   :file-selector {:opened false}
   })



;; -- Local Storage  ----------------------------------------------------------
;;
;; Part of the clchess challenge is to store todos in LocalStorage, and
;; on app startup, reload the todos from when the program was last run.
;; But we are not to load the setting for the "showing" filter. Just the todos.
;;

(def ls-key "clchess")     ;; localstore key

(defn themes->local-store
  "Puts theme into localStorage"
  [theme]
  (log/debug "themes->local-store")
  (.setItem js/localStorage ls-key (str theme)))   ;; sorted-map writen as an EDN map

;; register a coeffect handler which will load a value from localstore
;; To see it used look in events.clj at the event handler for `:initialise-db`
(re-frame/reg-cofx
 :local-store-themes
 (fn [cofx _]
   "Read in themes from localstore, and process into a map we can merge into app-db."
   (assoc cofx :local-store-themes
          (into (sorted-map)
                (some->> (.getItem js/localStorage ls-key)
                         (cljs.reader/read-string)       ;; stored as an EDN map.
                         (hash-map :theme)               ;; access via the :theme key
                         )))))
