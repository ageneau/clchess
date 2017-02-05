(ns clchess.db
  (:require [cljs.reader]
            [clchess.theme :as theme]
            [clchess.ctrl :as ctrl]
            [schema.core  :as s :include-macros true]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]))


;; -- Schema -----------------------------------------------------------------
;;
;; This is a Prismatic Schema which documents the structure of app-db
;; See: https://github.com/Prismatic/schema
;;
;; The value in app-db should ALWAYS match this schema. Now, the value in
;; app-db can ONLY be changed by event handlers so, after each event handler
;; has run, we re-check that app-db still matches this schema.
;;
;; How is this done? Look in handlers.cljs and you'll notice that all handers
;; have an "after" middleware which does the schema re-check.
;;
;; None of this is strictly necessary. It could be omitted. But we find it
;; good practice.

(def Theme {:is-2d s/Bool
            :theme (apply s/enum theme/theme-names)
            :data-theme (apply s/enum theme/data-themes)
            :data-theme-3d (apply s/enum theme/data-themes-3d)
            :data-set (apply s/enum theme/data-sets)
            :data-set-3d (apply s/enum theme/data-sets-3d)
            :background-img s/Str
            :zoom s/Str
            })

(def Square (apply s/enum ctrl/squares))

(def BoardMove [Square Square])

(def SquareKeyword (apply s/enum (map keyword ctrl/squares)))

(def Board {:viewOnly s/Bool
            :turnColor (s/enum "white" "black")
            :lastMove (s/enum BoardMove nil)
            :fen s/Str
            :movable {
                      :free s/Bool
                      :color (s/enum "white" "black" "both")
                      :premove s/Bool
                      :dests {SquareKeyword [Square]}
                      }
            :promotion {:show s/Bool
                        (s/optional-key :from) Square
                        (s/optional-key :to) Square
                        (s/optional-key :player) (s/enum "white" "black")}})

(def Piece (s/enum "q" "k" "b" "r"))

(def San s/Str)

(def Fen s/Str)

(def Move {:color (s/enum "w" "b")
           :from Square
           :to Square
           :flags s/Str
           :piece Piece
           :promotion Piece
           :san San
           :fen Fen})

(def Database {:key s/Str
               :name s/Str
               :type (s/enum :scid)
               :opened s/Bool})

(def Game {:initial-fen s/Str
           :moves [Move]
           :current-ply s/Int})

(def View {:is-full-screen s/Bool})

(def schema {:theme Theme
             :view View
             :board Board
             :game Game
             :databases {(s/optional-key :current) Database
                         (s/optional-key :all) {s/Str Database} }
             :file-selector {:opened s/Bool
                             (s/optional-key :action) (s/enum :load-pgn :open-db)
                             (s/optional-key :accept) s/Str}})



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
