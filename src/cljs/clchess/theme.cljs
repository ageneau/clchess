(ns clchess.theme
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classlist]
            [goog.events :as events]
            [goog.dom.dataset :as dataset]
            [clojure.string :as string]
            [clchess.utils :as utils]
            [clchess.widgets :as widgets]
            [cljs.spec :as s]
            [clchess.specs.theme :as stheme]
            [clchess.data.themes :as data]
            [taoensso.timbre :as log]))

(defn find-theme [name]
  {:pre [(s/valid? ::stheme/name name)]}
  (first (filter #(= name (:name %)) data/themes)))

(defn theme-2d-list [themes]
  [:div {:class "board"}
   (for [theme data/themes]
     ^{ :key theme } [:div {:class "theme" :data-theme theme} [:div {:class "color_demo blue"}]])])

(defn switch-theme! [new-theme]
  {:pre [(s/valid? ::stheme/name new-theme)]}
  (classlist/removeAll (utils/body) (clj->js data/theme-names))
  (classlist/add (utils/body) new-theme))

(defn switch-data-theme! [new-theme]
  {:pre [(s/valid? (s/or :data-theme ::stheme/data-theme
                         :data-theme-3d ::stheme/data-theme-3d) new-theme)]}
  (classlist/removeAll (utils/body) (clj->js data/data-themes))
  (classlist/removeAll (utils/body) (clj->js data/data-themes-3d))
  (classlist/add (utils/body) new-theme))

(defn switch-data-set! [new-set {:keys [is-2d]}]
  {:pre [(s/valid? (s/or :data-set ::stheme/data-set
                         :data-set-3d ::stheme/data-set-3d) new-set)]}
  (let [piece-css (dom/getElement "piece-sprite")
        board-css (dom/getElement "board-css")]
    (if is-2d
      (do
        (dom/setProperties piece-css #js {:href (str "lila/public/stylesheets/piece/" new-set ".css")})
        (dom/setProperties board-css #js {:href "lila/public/stylesheets/board.css" })
        (dataset/set (utils/body) "pieceSet" new-set))
      (do
        (classlist/removeAll (utils/body) (clj->js data/data-sets-3d))
        (dom/setProperties board-css #js {:href "lila/public/stylesheets/board-3d.css" })
        (classlist/add (utils/body) new-set)))))

(defn init-theme! [theme]
  {:pre [(s/valid? ::stheme/theme theme)]}
  (switch-theme! (::stheme/name theme))
  (if (::stheme/is-2d theme)
    (do
      (log/debug "init-theme 2d:" theme)
      (switch-data-theme! (::stheme/data-theme theme))
      (switch-data-set! (::stheme/data-set theme) {:is-2d true}))
    (do
      (log/debug "init-theme 3d:" theme)
      (switch-data-theme! (::stheme/data-theme-3d theme))
      (switch-data-set! (::stheme/data-set-3d theme) {:is-2d false}))))

(defn board-selector [{:keys [is-2d]}]
  (let [data-themes (if is-2d data/data-themes data/data-themes-3d)]
    [:div {:class "board"}
     (for [theme data-themes]
       ^{ :key theme }
       [:div {:class "theme" :data-theme theme
              :on-click #(dispatch [:theme/switch-theme
                                    (if is-2d ::stheme/data-theme ::stheme/data-theme-3d)
                                    theme])}
        [:div {:class (string/join " " ["color_demo" theme])}]])]))

(defn piece-selector [{:keys [is-2d]}]
  (let [data-sets (if is-2d data/data-sets data/data-sets-3d)]
    [:div {:class "piece_set"}
     (for [set data-sets]
       ^{ :key set }
       [:div {:class "no-square"
              :data-set set
              :on-click #(dispatch [:theme/switch-theme
                                    (if is-2d ::stheme/data-set ::stheme/data-set-3d)
                                    set])}
        [:piece {:class set}]])]))

(defn background-input []
  [:input {:data-href "/pref/bgImg"
           :type "text"
           :class "background_image"
           :value (::stheme/background-img @(subscribe [:theme]))
           :on-change #(dispatch [:theme/switch-theme
                                  ::stheme/background-img
                                  %])}])

(defn theme-selector-dropdown [theme]
  {:pre [(s/valid? ::stheme/theme theme)]}
  (fn [theme]
    (log/debug "theme-selector-dropdown: " (::stheme/name theme))
    [:div {:class "dropdown"
           :data-themes (string/join " " data/data-themes)
           :data-theme3ds (string/join " " data/data-themes-3d)
           :data-sets (string/join " " data/data-sets)
           :data-set3ds (string/join " " data/data-sets-3d)}
     [widgets/simple-toggle data/themes {:container-class "background"
                                         :on-toggle #(dispatch [:theme/switch-theme
                                                                ::stheme/name
                                                                (:name %)])
                                         :initial-value (find-theme (::stheme/name theme))}]
     (let [options [{:name "d2" :text "2D"}
                    {:name "d3" :text "3D"}]
           current (if (::stheme/is-2d theme) (first options) (second options))]
       [widgets/simple-toggle
        options
        {:container-class "dimensions"
         :on-toggle #(let [new-val (= "d2" (:name %))]
                       (log/debug "Switch dim:" new-val)
                       (dispatch-sync [:set-is-2d  new-val]))
         :initial-value current}])
     
     [background-input]
     ;; (widgets/slider (:zoom theme-state))
     (let [div-class (if (::stheme/is-2d theme) "is2d" "is3d")]
       [:div {:class div-class}
        [board-selector {:is-2d (::stheme/is-2d theme)}]
        [piece-selector {:is-2d (::stheme/is-2d theme)}]])]))

(defn theme-selector [theme]
  {:pre [(s/valid? ::stheme/theme theme)]}
  (let [shown (reagent/atom false)]
    (fn [theme]
      (log/debug "theme-selector")
      (let [is-shown @shown
            toggle [:a {:id "themepicker_toggle"
                        :class "toggle icon link hint--bottom-left"
                        :data-hint "Theming"
                        :data-url "/themepicker"
                        :on-click #(utils/handler-fn
                                    (log/debug "toggle")
                                    (reset! shown (not is-shown)))}
                    [:span {:data-icon "}"}]]]
        (if is-shown
          [:div {:id "themepicker" :class "fright shown"}
           toggle
           [theme-selector-dropdown theme]]
          [:div {:id "themepicker" :class "fright"}
           toggle])))))
