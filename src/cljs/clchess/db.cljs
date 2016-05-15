(ns clchess.db
  (:require [cljs.reader]
            [clchess.theme :as theme]
            [schema.core  :as s :include-macros true]))


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
            :theme s/Str
            :data-theme s/Str
            :data-theme-3d s/Str
            :data-set s/Str
            :data-set-3d s/Str
            :background-img s/Str
            :zoom s/Str
            })

(def schema {:theme Theme})



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
   })



;; -- Local Storage  ----------------------------------------------------------
;;
;; Part of the clchess challenge is to store todos in LocalStorage, and
;; on app startup, reload the todos from when the program was last run.
;; But we are not to load the setting for the "showing" filter. Just the todos.
;;

(def lsk "clchess")     ;; localstore key

(defn ls->theme
  "Read in theme from LS, and process into a map we can merge into app-db."
  []
  (some->> (.getItem js/localStorage lsk)
           (cljs.reader/read-string)   ;; stored as an EDN map.
           (hash-map :theme)))         ;; access via the :theme key

(defn theme->ls!
  "Puts theme into localStorage"
  [theme]
  (.setItem js/localStorage lsk (str theme)))   ;; sorted-map writen as an EDN map
