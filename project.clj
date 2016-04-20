(defproject reagent2 "0.1.0-SNAPSHOT"
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
                 [lein-figwheel "0.5.2"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [figwheel-sidecar "0.5.2"]
                 [com.cemerick/piggieback "0.2.1"]
                 [garden "1.3.0"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [cider/cider-nrepl "0.12.0-SNAPSHOT"]
            [lein-figwheel "0.5.2"]
            [lein-garden "0.2.6"]]

  :min-lein-version "2.5.0"

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :resource-paths ["public"]

  :cljsbuild {:builds {:app {:source-paths ["src" "env/dev/cljs"]
                             :compiler {:main "reagent2.dev"
                                        :output-to "public/js/app.js"
                                        :output-dir "public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true
                                        :source-map true}}}}

  :garden
  {:builds
   [{:id           "screen"
     :source-paths ["src-clj"]
     :stylesheet   reagent2.css/screen
     :compiler     {:output-to     "public/css/compiled/screen.css"
                    :pretty-print? true}}]}

  :figwheel {:http-server-root "public"
             :nrepl-port 7002
             :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
             :css-dirs ["public/css"]})
