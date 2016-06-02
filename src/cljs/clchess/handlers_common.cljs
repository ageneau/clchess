(ns clchess.handlers-common
  (:require
   [clojure.string :as string]
   [clchess.db    :refer [default-value ls->theme theme->ls! schema]]
   [re-frame.core :refer [dispatch register-handler path trim-v after debug]]
   [schema.core   :as s]
   [clchess.theme :as theme]
   [clchess.utils :as utils]
   [clchess.ctrl :as ctrl]
   [clchess.views :as views]
   [taoensso.timbre :as log]))


;; -- Middleware --------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/wiki/Using-Handler-Middleware
;;

(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (if-let [problems  (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))

;; Event handlers change state, that's their job. But what heppens if there's
;; a bug and they corrupt this state in some subtle way? This middleware is run after
;; each event handler has finished, and it checks app-db against a schema.  This
;; helps us detect event handler bugs early.
(def check-schema-mw (after (partial check-and-throw schema)))


;; middleware for any handler that manipulates themes
(def theme-middleware [check-schema-mw ;; ensure the schema is still valid
                       (path :theme)   ;; 1st param to handler will be value from this path
                       (after theme->ls!)            ;; write to localstore each time
                       (when ^boolean js/goog.DEBUG debug)       ;; look in your browser console
                       trim-v])        ;; remove event id from event vec


;; -- Event Handlers ----------------------------------------------------------
                                  ;; usage:  (dispatch [:initialise-db])
(register-handler                 ;; On app startup, ceate initial state
  :initialise-db                  ;; event id being handled
  check-schema-mw                 ;; afterwards: check that app-db matches the schema
  (fn [_ _]                       ;; the handler being registered
    (merge default-value (ls->theme))))  ;; all hail the new state

                                  ;; usage:  (dispatch [:set-is-2d  true])
(register-handler                 ;; this handler changes the 2d/3d view flag
  :set-is-2d                      ;; event-id
  theme-middleware  ;; middleware  (wraps the handler)

  ;; Because of the path middleware above, the 1st parameter to
  ;; the handler below won't be the entire 'db', and instead will
  ;; be the value at a certain path within db, namely :showing.
  ;; Also, the use of the 'trim-v' middleware means we can omit
  ;; the leading underscore from the 2nd parameter (event vector).
  (fn [old [new-val]]    ;; handler
    (assoc old :is-2d new-val)))         ;; return new state for the path

(register-handler
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

(register-handler
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

(register-handler
 :game/first-move
 [(path :game)
  trim-v]
 (fn [old _]
   (log/debug "First move:" old)
   (dispatch [:game/update-board])
   (assoc old :current-ply 0)))

(register-handler
 :game/last-move
 [(path :game)
  trim-v]
 (fn [old _]
   (let [{ current-ply :current-ply moves :moves } old ]
     (log/debug "Last move:" old)
     (dispatch [:game/update-board])
     (assoc old :current-ply (count moves)))))

(register-handler
 :game/set-board
 [(path :board)
  trim-v]
 (fn [old [board-state]]
   (assoc old :board board-state)))

(register-handler
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

(register-handler
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

(register-handler
 :menu/open-db
 [trim-v]
 (fn [old _]
   (log/debug "Open DB")
   (dispatch [:file/open-selector])
   (-> old
       (assoc-in [:file-selector :action] :open-db)
       (assoc-in [:file-selector :accept] (string/join "," [".si4" ".si3" ".pgn" ".PGN" ".pgn.gz"])))))

(register-handler
 :menu/load-pgn
 [trim-v]
 (fn [old _]
   (log/debug "Load pgn")
   (dispatch [:file/open-selector])
   (-> old
       (assoc-in [:file-selector :action] :load-pgn)
       (assoc-in [:file-selector :accept] ".pgn"))))

(register-handler
 :menu/reset-board
 [trim-v]
 (fn [old _]
   (log/debug "Reset board")
   old))

(register-handler
 :file/open-selector
 [(path :file-selector)
  trim-v]
 (fn [{opened :opened :as old} _]
   (views/open-file)
   (assoc old :opened true)))

(register-handler
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

(register-handler
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

