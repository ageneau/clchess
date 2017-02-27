(ns clchess.specs.clchess
  (:require #?(:clj [clojure.spec :as s]
               :cljs [cljs.spec :as s])
            [clchess.specs.view :as sview]
            [clchess.specs.chess :as schess]
            [clchess.specs.chessdb :as schessdb]
            [clchess.specs.theme :as stheme]
            [clchess.specs.chessground :as schessground]))

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


(s/def :file-selector/opened boolean?)
(s/def :file-selector/action #{:load-pgn :open-db})
(s/def :file-selector/accept string?)

(s/def ::file-selector (s/keys :req [:file-selector/opened]
                               :opt [:file-selector/action
                                     :file-selector/accept]))


(s/def ::db (s/keys :req [::stheme/theme
                          ::sview/view
                          ::schessground/board
                          ::schess/game
                          ::schessdb/databases
                          ::file-selector]))
