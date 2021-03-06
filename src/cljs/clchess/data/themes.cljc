(ns clchess.data.themes)

(def ^:const themes
  [{:name "light"
    :icon "5"}
   {:name "dark"
    :icon "4"}
   {:name "transp"
    :icon "l"}])

(def ^:const theme-names
  (into #{} (map #(:name %) themes)))

(def data-themes
  #{"blue" "blue2" "blue3" "canvas" "wood" "wood2" "wood3" "maple" "green" "marble" "brown" "leather" "grey" "metal" "olive" "purple"})

(def data-themes-3d
  #{"Black-White-Aluminium" "Brushed-Aluminium" "China-Blue" "China-Green" "China-Grey" "China-Scarlet" "Classic-Blue" "Gold-Silver" "Light-Wood" "Power-Coated" "Rosewood" "Marble" "Wax" "Jade" "Woodi"})

(def data-sets
  #{"cburnett" "merida" "alpha" "pirouetti" "chessnut" "chess7" "reillycraig" "companion" "fantasy" "spatial" "shapes"})

(def data-sets-3d
  #{"Basic" "Wood" "Metal" "RedVBlue" "ModernJade" "ModernWood" "Glass" "Trimmed" "Experimental"})

