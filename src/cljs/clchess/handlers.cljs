(ns clchess.handlers
  (:require
    [clchess.db    :refer [default-value ls->theme theme->ls! schema]]
    [re-frame.core :refer [register-handler path trim-v after debug]]
    [schema.core   :as s]
    [clchess.theme :as theme]
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


;; middleware for any handler that manipulates todos
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
