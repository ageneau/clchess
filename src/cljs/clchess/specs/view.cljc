(ns clchess.specs.view
  (:require #?(:clj [clojure.spec :as s]
               :cljs [cljs.spec :as s])))

(s/def ::is-fullscreen boolean?)
(s/def ::width int?)
(s/def ::height int?)

(s/def :window/size (s/keys :req-un [::width
                                     ::height]))

(s/def ::view (s/keys :req [::is-fullscreen]
                      :opt [:window/size]))
