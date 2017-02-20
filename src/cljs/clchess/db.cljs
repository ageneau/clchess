(ns clchess.db
  (:require [cljs.reader]
            [cljs.spec :as s]
            [clchess.specs.clchess :as app-spec]
            [clchess.specs.chess :as schess]
            [clchess.specs.theme :as stheme]
            [clchess.specs.chessdb :as schessdb]
            [clchess.specs.view :as sview]
            [clchess.specs.board :as sboard]
            [re-frame.core :as re-frame]
            [cognitect.transit :as t]
            [taoensso.timbre :as log]))

;; -- Default app-db Value  ---------------------------------------------------
;;
;; When the application first starts, this will be the value put in app-db
;; Unless, or course, there are todos in the LocalStore (see further below)
;; Look in core.cljs for  "(dispatch-sync [:initialise-db])"
;;

(def default-value            ;; what gets put into app-db by default.
  {::stheme/theme {::stheme/is-2d true
                   ::stheme/name "light"
                   ::stheme/data-theme "blue"
                   ::stheme/data-theme-3d "Black-White-Aluminium"
                   ::stheme/data-set "cburnett"
                   ::stheme/data-set-3d "Basic"
                   ::stheme/background-img "http://lichess1.org/assets/images/background/landscape.jpg"
                   ::stheme/zoom "80%"}
   ::sview/view {::sview/is-fullscreen false}
   ::sboard/board {:viewOnly false
                   :turnColor "white"
                   :lastMove nil
                   :fen "start"
                   :movable {:free false
                             :color "both"
                             :premove true
                             :dests {}}
                   :promotion {:show false}}
   ::schess/game {::schess/initial-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                  ::schess/moves []
                  ::schess/current-ply 0}
   ::schessdb/databases {}
   ::app-spec/file-selector {:file-selector/opened false}
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

  (let [w (t/writer :json)]
    (log/debug "themes->local-store")
    (.setItem js/localStorage ls-key (t/write w theme))))   ;; sorted-map writen as an EDN map

;; register a coeffect handler which will load a value from localstore
;; To see it used look in events.clj at the event handler for `:initialise-db`
(re-frame/reg-cofx
 :local-store-themes
 (fn [cofx _]
   "Read in themes from localstore, and process into a map we can merge into app-db."
   (let [r (t/reader :json)]
     (assoc cofx :local-store-themes
            (into (sorted-map)
                  (some->> (.getItem js/localStorage ls-key)
                           (t/read r)
                           ))))))
