(ns clchess.handlers-common
  (:require
   [clojure.string :as string]
   [clchess.db    :refer [default-value schema themes->local-store]]
   [re-frame.core :refer [dispatch dispatch-sync reg-event-db reg-event-fx inject-cofx path trim-v after debug]]
   [schema.core   :as s]
   [clchess.theme :as theme]
   [clchess.utils :as utils]
   [clchess.ctrl :as ctrl]
   [clchess.views :as views]
   [taoensso.timbre :as log]))


;; -- Interceptors --------------------------------------------------------------
;;

(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (log/debug "check-and-throw, DB:" db)
  (if-let [problems  (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))

;; Event handlers change state, that's their job. But what happens if there's
;; a bug which corrupts app state in some subtle way? This interceptor is run after
;; each event handler has finished, and it checks app-db against a schema.  This
;; helps us detect event handler bugs early.
(def check-schema-interceptor (after (partial check-and-throw schema)))

(def ->local-store (after themes->local-store))

;; interceptor for any handler that manipulates themes
(def theme-interceptors [check-schema-interceptor ;; ensure the schema is still valid
                         (path :theme)   ;; 1st param to handler will be value from this path
                         ->local-store            ;; write to localstore each time
                         (when ^boolean js/goog.DEBUG debug)       ;; look in your browser console
                         trim-v])        ;; remove event id from event vec


;; -- Event Handlers ----------------------------------------------------------
                                  ;; usage:  (dispatch [:initialise-db])
(reg-event-fx                 ;; On app startup, ceate initial state
  :initialise-db                  ;; event id being handled
  [(inject-cofx :local-store-themes)
   check-schema-interceptor
   ]                 ;; afterwards: check that app-db matches the schema
  (fn [{:keys [db local-store-themes]} _]                    ;; the handler being registered
    (log/debug "Theme:" local-store-themes "DB: " (merge-with merge default-value  {:theme local-store-themes}))
    {:db (merge-with merge default-value  {:theme local-store-themes}) }))  ;; all hail the new state

                                  ;; usage:  (dispatch [:set-is-2d  true])
(reg-event-db                 ;; this handler changes the 2d/3d view flag
  :set-is-2d                      ;; event-id
  theme-interceptors  ;; interceptor  (wraps the handler)

  ;; Because of the path interceptor above, the 1st parameter to
  ;; the handler below won't be the entire 'db', and instead will
  ;; be the value at a certain path within db, namely :showing.
  ;; Also, the use of the 'trim-v' interceptor means we can omit
  ;; the leading underscore from the 2nd parameter (event vector).
  (fn [old [new-val]]    ;; handler
    (assoc old :is-2d new-val)))         ;; return new state for the path

(reg-event-db
 :game/next-move
 [(path :game)
  trim-v]
 (fn [old _]
   (let [{ current-ply :current-ply moves :moves } old]
     (if (= current-ply (count moves))
       (do
         (log/debug "End of game")
         old)
       (do
         (log/debug "Next move:" old)
         (dispatch [:game/update-board])
         (assoc old :current-ply (inc (:current-ply old))))))))

(reg-event-db
 :game/previous-move
 [(path :game)
  trim-v]
 (fn [old _]
   (let [{ current-ply :current-ply moves :moves } old]
     (if (= current-ply 0)
       (do
         (log/debug "Beggining of game")
         old)
       (do
         (log/debug "Previous move:" old)
         (dispatch [:game/update-board])
         (assoc old :current-ply (dec (:current-ply old))))))))

(reg-event-db
 :game/first-move
 [(path :game)
  trim-v]
 (fn [old _]
   (log/debug "First move:" old)
   (dispatch [:game/update-board])
   (assoc old :current-ply 0)))

(reg-event-db
 :game/last-move
 [(path :game)
  trim-v]
 (fn [old _]
   (let [{ current-ply :current-ply moves :moves } old ]
     (log/debug "Last move:" old)
     (dispatch [:game/update-board])
     (assoc old :current-ply (count moves)))))

(reg-event-db
 :game/set-board
 [(path :board)
  trim-v]
 (fn [old [board-state]]
   (assoc old :board board-state)))

(reg-event-db
 :game/board-move
 [trim-v]
 (fn [old [from to { promoting :promoting player :player :as flags } :as move]]
   (let [game (:game old)
         board (:board old)
         { current-ply :current-ply moves :moves } game]
     (log/debug "Board move:" from "," to "," current-ply ", flags:" flags ", promoting: " promoting)
     (cond
       (not= current-ply (count moves))
       (do
         (log/debug "Not at the end of the move list")
         old)

       promoting
       (let [promotion {:show true :from from :to to :player player}]
         (log/debug "Promoting:" (:board (assoc-in old [:board :promotion] promotion)))
         (assoc-in old [:board :promotion] promotion))

       :else
       (let [game-state (ctrl/make-move game from to)
             updated-state (update-in old [:game] merge game-state)]
         (log/debug "Make move:" game-state)
         (dispatch [:game/update-board])
         updated-state)))))

(reg-event-db
 :game/update-board
 [trim-v]
 (fn [old _]
   (let [{board :board game :game} old
         {fen :fen
          color :color
          dest-squares :dest-squares
          last-move :last-move
          :as state} (ctrl/compute-state game)
         updated-board (-> old
                           (assoc-in [:board :turnColor] color)
                           (assoc-in [:board :lastMove] (when last-move
                                                          [(:from last-move) (:to last-move)]))
                           (assoc-in [:board :fen] fen)
                           (assoc-in [:board :movable :dests] dest-squares))]
     (log/debug "Update board:" (:board updated-board))
     updated-board)))

(reg-event-db
 :menu/open-db
 [trim-v]
 (fn [old _]
   (log/debug "Open DB")
   (dispatch [:file/open-selector])
   (-> old
       (assoc-in [:file-selector :action] :open-db)
       (assoc-in [:file-selector :accept] (string/join "," [".si4" ".si3" ".pgn" ".PGN" ".pgn.gz"])))))

(reg-event-db
 :menu/load-pgn
 [trim-v]
 (fn [old _]
   (log/debug "Load pgn")
   (dispatch [:file/open-selector])
   (-> old
       (assoc-in [:file-selector :action] :load-pgn)
       (assoc-in [:file-selector :accept] ".pgn"))))

(reg-event-db
 :menu/reset-board
 [trim-v]
 (fn [old _]
   (log/debug "Reset board")
   old))

(reg-event-db
 :file/open-selector
 [(path :file-selector)
  trim-v]
 (fn [{opened :opened :as old} _]
   (views/open-file)
   (assoc old :opened true)))

(reg-event-db
 :file/changed
 [trim-v]
 (fn [db [action file]]
   (log/debug "File changed: " action ", " file)
   (let [db (assoc-in db [:file-selector :opened] false)]
     (case action
       :load-pgn
       (let [{game :game} db
             file (utils/read-file file)
             new-state (ctrl/load-pgn game file)]
         (dispatch [:game/update-board])
         (update-in db [:game] merge new-state))

       :open-db
       (do (dispatch [:db/open file])
           db)))))

(reg-event-db
 :game/promote-to
 [trim-v]
 (fn [old [piece]]
   (log/debug "Promote to: " piece)
   (let [game (:game old)
         board (:board old)
         {from :from to :to } (:promotion board)
         new-state (ctrl/make-move game from to :promotion piece)]
     (log/debug "Make move:" new-state ", NEW STATE:" (update-in old [:game] merge new-state))
     (dispatch [:game/update-board])
     (-> old
         (update-in [:game] merge new-state)
         (assoc-in [:board :promotion] {:show false})))))

