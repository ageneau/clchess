(ns clchess.events-common
  (:require
   [clojure.string :as string]
   [clchess.db    :refer [default-value themes->local-store]]
   [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx reg-fx inject-cofx path trim-v after debug]]
   [cljs.spec :as s]
   [clchess.specs.theme :as stheme]
   [clchess.specs.board :as sboard]
   [clchess.specs.chess :as schess]
   [clchess.specs.view :as sview]
   [clchess.specs.chessdb :as schessdb]
   [clchess.theme :as theme]
   [clchess.board :as board]
   [clchess.utils :as utils]
   [clchess.ctrl :as ctrl]
   [clchess.views :as views]
   [taoensso.timbre :as log]))


;; -- Interceptors --------------------------------------------------------------
;;

(defn check-and-throw
  "throw an exception if db doesn't match the spec"
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;; Event handlers change state, that's their job. But what happens if there's
;; a bug which corrupts app state in some subtle way? This interceptor is run after
;; each event handler has finished, and it checks app-db against a spec.  This
;; helps us detect event handler bugs early.
(def check-spec-interceptor (after (partial check-and-throw :clchess.specs.clchess/db)))

(def ->local-store (after themes->local-store))

;; interceptor for any handler that manipulates themes
(def theme-interceptors [check-spec-interceptor ;; ensure the spec is still valid
                         (path ::stheme/theme)   ;; 1st param to handler will be value from this path
                         ->local-store            ;; write to localstore each time
                         (when ^boolean js/goog.DEBUG debug)       ;; look in your browser console
                         trim-v])        ;; remove event id from event vec


(def game-interceptors [check-spec-interceptor
                        (path ::schess/game)
                        (when ^boolean js/goog.DEBUG debug)
                        trim-v])

(def board-interceptors [check-spec-interceptor
                         (path ::sboard/board)
                         (when ^boolean js/goog.DEBUG debug)
                         trim-v])

(def generic-interceptor [check-spec-interceptor
                          (when ^boolean js/goog.DEBUG debug)
                          trim-v])

(def file-interceptor [check-spec-interceptor
                       (path :file-selector)
                       (when ^boolean js/goog.DEBUG debug)
                       trim-v])

(def view-interceptor [check-spec-interceptor
                       (path ::sview/view)
                       (when ^boolean js/goog.DEBUG debug)
                       trim-v])

(def databases-interceptor [check-spec-interceptor
                            (path ::schessdb/databases)
                            (when ^boolean js/goog.DEBUG debug)
                            trim-v])

;; -- Event Handlers ----------------------------------------------------------
                                  ;; usage:  (dispatch [:initialise-db])
(reg-event-fx                     ;; On app startup, create initial state
  :initialise-db                  ;; event id being handled
  [(inject-cofx :local-store-themes)
   check-spec-interceptor]                                   ;; afterwards: check that app-db matches the spec
  (fn [{:keys [db local-store-themes]} _]                    ;; the handler being registered
    (let [theme (merge (::stheme/theme default-value)
                       local-store-themes)
          db-new (assoc default-value ::stheme/theme theme)]
      {:db db-new
       :theme/initialize [theme]})))  ;; all hail the new state

                                  ;; usage:  (dispatch [:set-is-2d  true])
(reg-event-fx                 ;; this handler changes the 2d/3d view flag
  :set-is-2d                      ;; event-id
  theme-interceptors  ;; interceptor  (wraps the handler)

  ;; Because of the path interceptor above, the 1st parameter to
  ;; the handler below won't be the entire 'db', and instead will
  ;; be the value at a certain path within db, namely :showing.
  ;; Also, the use of the 'trim-v' interceptor means we can omit
  ;; the leading underscore from the 2nd parameter (event vector).
  (fn [{:keys [db]} [new-val]]    ;; handler
    (log/debug "In set-is-2d:" db)
    (let [theme (assoc db ::stheme/is-2d new-val)]
      {:db theme
       :theme/initialize [theme]
       :dispatch-later [{:ms 100 :dispatch [:board/force-resize]}]
       })))

(reg-fx
 :board/force-resize
 (fn [_]
   (board/force-resize)))

(reg-event-fx
 :board/force-resize
 board-interceptors
 (fn [cofx _]
   (log/debug "Forcing board resize")
   {:board/force-resize nil}))

(reg-event-fx
 :game/next-move
 game-interceptors
 (fn [cofx _]
   (let [{ current-ply ::schess/current-ply moves ::schess/moves } (:db cofx)]
     (if (= current-ply (count moves))
       (do
         (log/debug "End of game")
         {:db (:db cofx)})
       (do
         (log/debug "Next move:" (:db cofx))
         {:db (assoc (:db cofx) ::schess/current-ply (inc (::schess/current-ply (:db cofx))))
          :dispatch [:game/update-board]})))))

(reg-event-fx
 :game/previous-move
 game-interceptors
 (fn [cofx _]
   (let [{ current-ply ::schess/current-ply moves ::schess/moves } (:db cofx)]
     (if (= current-ply 0)
       (do
         (log/debug "Beggining of game")
         {:db (:db cofx)})
       (do
         (log/debug "Previous move:" (:db cofx))
         {:db (assoc (:db cofx) ::schess/current-ply (dec (::schess/current-ply (:db cofx))))
          :dispatch [:game/update-board]})))))

(reg-event-fx
 :game/first-move
 game-interceptors
 (fn [cofx _]
   (log/debug "First move:" (:db cofx))
   {:db (assoc (:db cofx) ::schess/current-ply 0)
    :dispatch [:game/update-board]}))

(reg-event-fx
 :game/last-move
 game-interceptors
 (fn [cofx _]
   (let [{ current-ply ::schess/current-ply moves ::schess/moves } (:db cofx) ]
     (log/debug "Last move:" (:db cofx))
     {:db (assoc (:db cofx) ::schess/current-ply (count moves))
      :dispatch [:game/update-board]})))

(reg-event-db
 :game/set-board
 board-interceptors
 (fn [db [board-state]]
   (assoc db ::sboard/board board-state)))

(reg-event-fx
 :game/board-move
 generic-interceptor
 (fn [cofx [from to { promoting :promoting player :player :as flags } :as move]]
   (let [game (::schess/game (:db cofx))
         board (::sboard/board (:db cofx))
         { current-ply ::schess/current-ply moves ::schess/moves } game]
     (log/debug "Board move:" from "," to "," current-ply ", flags:" flags ", promoting: " promoting)
     (cond
       (not= current-ply (count moves))
       (do
         (log/debug "Not at the end of the move list")
         {:db (:db cofx)})

       promoting
       (let [promotion {:show true :from from :to to :player player}]
         (log/debug "Promoting:" (::sboard/board (assoc-in (:db cofx) [::sboard/board :promotion] promotion)))
         {:db (assoc-in (:db cofx) [::sboard/board :promotion] promotion)})

       :else
       (let [game-state (ctrl/make-move game from to)
             updated-state (update-in (:db cofx) [::schess/game] merge game-state)]
         (log/debug "Make move:" game-state)
         {:db updated-state
          :dispatch [:game/update-board]})))))

(reg-event-db
 :game/update-board
 generic-interceptor
 (fn [db _]
   (let [{board ::sboard/board game ::schess/game} db
         {fen ::schess/fen
          color ::schess/color
          dest-squares ::sboard/dests
          last-move ::schess/last-move
          :as state} (ctrl/compute-state game)
         updated-board (-> db
                           (assoc-in [::sboard/board :turnColor] color)
                           (assoc-in [::sboard/board :lastMove] (when last-move
                                                                  [(::schess/from last-move)
                                                                   (::schess/to last-move)]))
                           (assoc-in [::sboard/board :fen] fen)
                           (assoc-in [::sboard/board :movable :dests] dest-squares))]
     (log/debug "Update board:" (::sboard/board updated-board))
     updated-board)))

(reg-event-fx
 :menu/open-db
 generic-interceptor
 (fn [cofx _]
   (log/debug "Open DB")
   {:db (-> (:db cofx)
            (assoc-in [:file-selector :action] :open-db)
            (assoc-in [:file-selector :accept] (string/join "," [".si4" ".si3" ".pgn" ".PGN" ".pgn.gz"])))
    :dispatch [:file/open-selector]}))

(reg-event-fx
 :menu/load-pgn
 generic-interceptor
 (fn [cofx _]
   (log/debug "Load pgn")
   {:db (-> (:db cofx)
            (assoc-in [:file-selector :action] :load-pgn)
            (assoc-in [:file-selector :accept] ".pgn"))
    :dispatch [:file/open-selector]}))

(reg-event-db
 :menu/reset-board
 generic-interceptor
 (fn [db _]
   (log/debug "Reset board")
   db))

(reg-event-db
 :file/open-selector
 file-interceptor
 (fn [{opened :opened :as db} _]
   (views/open-file)
   (assoc db :opened true)))

(reg-event-fx
 :file/changed
 generic-interceptor
 (fn [cofx [action file]]
   (log/debug "File changed: " action ", " file)
   (let [db (assoc-in (:db cofx) [:file-selector :opened] false)]
     (case action
       :load-pgn
       (let [{game ::schess/game} db
             file (utils/read-file file)
             new-state (ctrl/load-pgn game file)]
         {:db (update-in db [::schess/game] merge new-state)
          :dispatch [:game/update-board]})

       :open-db
       {:dispatch [:db/open file]}))))

(reg-event-fx
 :game/promote-to
 generic-interceptor
 (fn [cofx [piece]]
   (log/debug "Promote to: " piece)
   (let [game (::schess/game (:db cofx))
         board (::sboard/board (:db cofx))
         {from :from to :to } (:promotion board)
         new-state (ctrl/make-move game from to ::schess/promotion piece)]
     (log/debug "Make move:" new-state ", NEW STATE:" (update-in (:db cofx) [::schess/game] merge new-state))
     {:db (-> (:db cofx)
              (update-in [::schess/game] merge new-state)
              (assoc-in [::sboard/board :promotion] {:show false}))
      :dispatch [:game/update-board]})))

(reg-event-db
 :view/resized
 view-interceptor
 (fn [db _]
   (let [size {:width (.-innerWidth js/window)
               :height (.-innerHeight js/window)}]
     (assoc db :size size))))

(reg-fx
 :theme/switch-theme
 (fn [[type theme]]
   {:pre [(s/valid? #{::stheme/name
                      ::stheme/background-img
                      ::stheme/data-theme
                      ::stheme/data-theme-3d
                      ::stheme/data-set
                      ::stheme/data-set-3d} type)]}
   (log/debug "theme type: " type ", theme:" theme)
   (case type
     ::stheme/name (theme/switch-theme! theme)
     ::stheme/background-img nil
     ::stheme/data-theme (theme/switch-data-theme! theme)
     ::stheme/data-theme-3d (theme/switch-data-theme! theme)
     ::stheme/data-set (theme/switch-data-set! theme {:is-2d true})
     ::stheme/data-set-3d (theme/switch-data-set! theme {:is2d false}))))

(reg-fx
 :theme/initialize
 (fn [[theme]]
   {:pre [(s/valid? ::stheme/theme theme)]}
   (theme/init-theme! theme)))

(reg-event-fx
 :theme/switch-theme
 theme-interceptors
 (fn [cofx [type theme]]
   (log/debug ":theme/switch-theme: type:" type ", theme" theme ", (:db cofx)" (assoc (:db cofx) type theme))
   {:db (assoc (:db cofx) type theme)
    :theme/switch-theme [type theme]}))

(reg-event-fx
 :theme/initialize
 theme-interceptors
 (fn [cofx [theme]]
   {:pre [(s/valid? ::stheme/theme theme)]}
   (log/debug ":theme/initialize: " theme ", (:db cofx)" (:db cofx))
   {:theme/initialize [theme]}))
