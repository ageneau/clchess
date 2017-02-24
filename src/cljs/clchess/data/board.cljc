(ns clchess.data.board)

;; FIXME: move to utils
(defn char-range [start end]
  #?(:cljs (map char (range (.charCodeAt start 0)
                            (inc (.charCodeAt end 0))))
     :clj (map char (range (int start)
                           (inc (int end))))))

(def ^:const files #?(:cljs (char-range "a" "h")
                      :clj (char-range \a \h)))

(def ^:const rows #?(:cljs (char-range "1" "8")
                     :clj (char-range \1 \8)))

(def ^:const squares
  (into #{} (for [file files
                  row rows]
              (str file row))))
