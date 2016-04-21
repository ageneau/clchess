(defproject clchess "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                 [org.clojure/clojurescript "1.8.40" :scope "provided"]
                 [reagent "0.5.1"]
                 [reagent-forms "0.5.22"]
                 [reagent-utils "0.1.7"]
                 [secretary "1.2.3"]
                 [prone "1.1.0"]
                 [com.taoensso/timbre "4.3.1"]
                 [lein-figwheel "0.5.2"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [figwheel-sidecar "0.5.2"]
                 [com.cemerick/piggieback "0.2.1"]
                 [garden "1.3.0"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [cider/cider-nrepl "0.12.0"]
            [lein-figwheel "0.5.2"]
            [lein-garden "0.2.6"]]

  :min-lein-version "2.5.0"

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :resource-paths ["resources/public"]

  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "env/dev/cljs"]
                             :compiler {:main "clchess.dev"
                                        :output-to "resources/public/js/app.js"
                                        :output-dir "resources/public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true
                                        :source-map true}}}}

  :garden
  {:builds
   [{:id           "clchess"
     :source-paths ["src/clj"]
     :stylesheet   clchess.css/clchess
     :compiler     {:output-to     "resources/public/css/compiled/clchess.css"
                    :pretty-print? true}}]}

  :figwheel {:nrepl-port 7002
             :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
             :css-dirs ["resources/public/css"]})
