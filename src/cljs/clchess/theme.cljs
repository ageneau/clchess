(ns clchess.theme
  (:require [reagent.core :as reagent :refer [atom]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classlist]
            [goog.events :as events]
            [goog.dom.dataset :as dataset]
            [clojure.string :as string]
            [clchess.utils :as utils]
            [clchess.widgets :as widgets]
            [taoensso.timbre :as log]))

(def themes
  [{:name "light"
    :icon "5"}
   {:name "dark"
    :icon "4"}
   {:name "transp"
    :icon "l"}])

(defn theme-list []
  (map #(:name %1) themes))

(def data-themes
  ["blue" "blue2" "blue3" "canvas" "wood" "wood2" "wood3" "maple" "green" "marble" "brown" "leather" "grey" "metal" "olive" "purple"])

(def data-themes-3d
  ["Black-White-Aluminium" "Brushed-Aluminium" "China-Blue" "China-Green" "China-Grey" "China-Scarlet" "Classic-Blue" "Gold-Silver" "Light-Wood" "Power-Coated" "Rosewood" "Marble" "Wax" "Jade" "Woodi"])

(def data-sets
  ["cburnett" "merida" "alpha" "pirouetti" "chessnut" "chess7" "reillycraig" "companion" "fantasy" "spatial" "shapes"])

(def data-sets-3d
  ["Basic" "Wood" "Metal" "RedVBlue" "ModernJade" "ModernWood" "Glass" "Trimmed" "Experimental"])

(defonce theme-state
  (reagent/atom
   {:is-2d true
    :theme (first themes)
    :data-theme (first data-themes)
    :data-theme-3d (first data-themes-3d)
    :data-set (first data-sets)
    :data-set-3d (first data-sets-3d)
    :background-img "http://lichess1.org/assets/images/background/landscape.jpg"
    :zoom "80%"}))

(defn theme-2d-list [themes]
  [:div {:class "board"}
   (for [theme themes]
     ^{ :key theme } [:div {:class "theme" :data-theme theme} [:div {:class "color_demo blue"}]])])

(defn switch-theme! [new-theme]
  (classlist/removeAll (utils/body) (clj->js (theme-list)))
  (classlist/add (utils/body) new-theme))

(defn switch-data-theme! [new-theme]
  (classlist/removeAll (utils/body) (clj->js data-themes))
  (classlist/removeAll (utils/body) (clj->js data-themes-3d))
  (classlist/add (utils/body) new-theme))

(defn switch-data-set! [new-set {:keys [is-2d]}]
  (let [css (dom/getElement "piece-sprite")]
    (if is-2d
      (do
        (dom/setProperties css #js {:href (str "lila/public/stylesheets/piece/" new-set ".css")})
        (dataset/set (utils/body) "pieceSet" new-set))
      (do
        (classlist/removeAll (utils/body) (clj->js data-sets-3d))
        (classlist/add (utils/body) new-set)))))

(defn init-theme []
  (let [theme @theme-state]
    (log/info "init-theme:" theme)
    (switch-theme! (:name (theme :theme)))
    (if (theme :is-2d)
      (do
        (switch-data-theme! (theme :data-theme))
        (switch-data-set! (theme :data-set) {:is-2d true}))
      (do
        (switch-data-theme! (theme :data-theme-3d))
        (switch-data-set! (theme :data-set-3d) {:is-2d false})))))

(defn board-selector [{:keys [is-2d]}]
  (let [data-themes (if is-2d data-themes data-themes-3d)]
    [:div {:class "board"}
     (for [theme data-themes]
       ^{ :key theme }
       [:div {:class "theme" :data-theme theme
              :on-click #(do
                           (log/info "Select:" theme)
                           (swap! theme-state assoc (if is-2d :data-theme :data-theme-3d) theme)
                           (switch-data-theme! theme))}
        [:div {:class (string/join " " ["color_demo" theme])}]])]))

(defn piece-selector [{:keys [is-2d]}]
  (let [data-sets (if is-2d data-sets data-sets-3d)]
    [:div {:class "piece_set"}
     (for [set data-sets]
       ^{ :key set }
       [:div {:class "no-square"
              :data-set set
              :on-click #(do
                           (log/info "Select set:" set)
                           (swap! theme-state assoc (if is-2d :data-set :data-set-3d) set)
                           (switch-data-set! set {:is-2d is-2d}))}
        [:piece {:class set}]])]))

(defn background-input []
  [:input {:data-href "/pref/bgImg"
           :type "text"
           :class "background_image"
           :value (@theme-state :background-img)
           :on-change #(do
                         (js/info "Background:" %1)
                         (swap! theme-state assoc :background-img %1))}])

(defn theme-selector-dropdown []
  [:div {:class "dropdown"
         :data-themes (string/join " " data-themes)
         :data-theme3ds (string/join " " data-themes-3d)
         :data-sets (string/join " " data-sets)
         :data-set3ds (string/join " " data-sets-3d)}
   [widgets/simple-toggle themes {:container-class "background"
                                  :on-toggle #(switch-theme! (:name %1))
                                  :initial-value (:theme @theme-state)}]
   (let [options [{:name "d2" :text "2D"}
                  {:name "d3" :text "3D"}]
         current (if (:is-2d @theme-state) (first options) (second options))]
     [widgets/simple-toggle
      options
      {:container-class "dimensions"
       :on-toggle #(let [new-val (= "d2" (%1 :name))]
                     (log/info "Switch:" %1)
                     (swap! theme-state assoc :is-2d new-val)
                     (init-theme))
       :initial-value current}])
   
   [background-input]
   ;; (widgets/slider (:zoom theme-state))
   (let [is-2d (@theme-state :is-2d)
         div-class (if is-2d "is2d" "is3d")]
     [:div {:class div-class}
      [board-selector {:is-2d is-2d}]
      [piece-selector {:is-2d is-2d}]])])

(defn theme-selector []
  (let [shown (reagent/atom false)]
    (fn []
      (let [is-shown @shown
            toggle [:a {:id "themepicker_toggle"
                        :class "toggle icon link hint--bottom-left"
                        :data-hint "Theming"
                        :data-url "/themepicker"
                        :on-click #(utils/handler-fn
                                    (log/info "toggle")
                                    (reset! shown (not is-shown)))}
                    [:span {:data-icon "}"}]]]
        (if is-shown
          [:div {:id "themepicker" :class "fright shown"}
           toggle
           [theme-selector-dropdown]]
          [:div {:id "themepicker" :class "fright"}
           toggle])))))
