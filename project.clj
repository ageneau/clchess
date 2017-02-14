(defproject clchess "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2017
            :key "mit"}

  :min-lein-version "2.6.1"

  :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                 [org.clojure/clojurescript "1.9.473" :scope "provided"]
                 [org.clojure/core.async "0.2.395"]
                 [reagent "0.6.0"]
                 [reagent-forms "0.5.28"]
                 [reagent-utils "0.2.0"]
                 [re-frame "0.9.2"]
                 [secretary "1.2.3"]
                 [prone "1.1.4"]
                 [com.taoensso/timbre "4.8.0"]
                 [com.cemerick/piggieback "0.2.1"]
                 [garden "1.3.2"]
                 [jamesmacaulay/cljs-promises "0.1.0"]

                 ;; dev dependencies
                 [figwheel-sidecar "0.5.9"]
                 [binaryage/devtools "0.9.1"]
                 [re-frisk "0.3.2"]]

  :plugins [[lein-cljsbuild "1.1.5" :exclusions [org.apache.commons/commons-compress]]
            [lein-figwheel "0.5.9"]
            [lein-garden "0.3.0"]
            [lein-node-webkit-build "0.1.8" :exclusions [com.fasterxml.jackson.core/jackson-annotations]]
            [lein-doo "0.1.7"]
            [lein-license "0.1.6"]]

  :doo {:build "test"}

  :node-webkit-build {
                      :root "resources/public" ; your node-webkit app root directory
                      ;; :name nil ; use this to override the application name
                      ;; :version nil ; use this to override the application version
                      ;; :osx {
                      ;;       :icon nil ; point to an .icns icon file to be used on the generated mac osx build
                      ;;       }
                      :platforms #{
                                   ;; :osx
                                   ;; :osx64
                                   ;; :win
                                   ;; :linux32
                                   :linux64
                                   } ; select which platforms to generate the build
                      :nw-version "0.20.0" ; the node-webkit version to be used :latest for latest
                      :output "releases" ; output directory for the generated builds
                      :disable-developer-toolbar true ; this will update your package.json to remove the developer toolbar
                      :use-lein-project-version true ; update the project version using your leiningen project version
                      ;; :tmp-path (path-join "tmp" "nw-build") ; temporary path to place intermediate build files
                      }

  :clean-targets ^{:protect false}
  ["resources/public/js/compiled"
   "resources/public/css/compiled"
   "target"]

  :resource-paths ["resources/public"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs" "src/node" "env/dev/cljs"]
                :compiler {:main "clchess.dev"
                           :output-to "resources/public/js/compiled/clchess.js"
                           :output-dir "resources/public/js/compiled/out"
                           :asset-path   "js/compiled/out"
                           :optimizations :none
                           :pretty-print  true
                           :source-map true}}
               {:id "test"
                :source-paths ["src/cljs" "test/cljs"]
                :compiler
                {:output-to "resources/public/js/compiled/out/testable.js"
                 :main clchess.test-runner
                 :optimizations :none}}
               {:id "web"
                :source-paths ["src/cljs" "src/web" "env/dev/cljs"]
                :compiler {:main "clchess.dev"
                           :output-to "resources/public/js/compiled/clchess_web.js"
                           :output-dir "resources/public/js/compiled/out-web"
                           :asset-path   "js/compiled/out-web"
                           :optimizations :none
                           :pretty-print  true
                           :source-map true}
                }
               {:id "prod"
                :source-paths ["src/cljs" "src/node" "env/prod/cljs"]
                :compiler {:main "clchess.prod"
                           :output-to "resources/public/js/compiled/clchess.js"
                           :output-dir "resources/public/js/compiled/out-prod"
                           :optimizations :advanced
                           :pretty-print false}}]}

  :garden {:builds
           [{:id           "clchess"
             :source-paths ["src/garden"]
             :stylesheet   clchess.css/clchess
             :compiler     {:output-to     "resources/public/css/compiled/clchess.css"
                            :pretty-print? true}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             :open-file-command "figwheel_edit"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             })
