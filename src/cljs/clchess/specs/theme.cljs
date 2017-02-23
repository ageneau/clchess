(ns clchess.specs.theme
  (:require [cljs.spec :as s]
            [clchess.data.clchess :as data]))

(s/def ::is-2d boolean?)
(s/def ::name data/theme-names)
(s/def ::data-theme data/data-themes)
(s/def ::data-theme-3d data/data-themes-3d)
(s/def ::data-set data/data-sets)
(s/def ::data-set-3d data/data-sets-3d)
(s/def ::background-img (s/nilable string?))
(s/def ::zoom string?)

(s/def ::theme (s/keys :req [::is-2d
                             ::name
                             ::data-theme
                             ::data-theme-3d
                             ::data-set
                             ::data-set-3d
                             ::background-img
                             ::zoom]))
