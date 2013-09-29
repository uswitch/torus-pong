(defproject torus-pong "0.1.0-SNAPSHOT"
  :description "A multiplayer take on the classic Game of Pong. Entry for Clojure Cup 2013."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1909"]

                 ;; because we needed to be on the bleeding edge and
                 ;; latest snapshot was nowhere to be found
                 [ragge/core.async "0.1.0-SNAPSHOT"]

                 [ring/ring-jetty-adapter "1.2.0"]
                 [ring/ring-servlet "1.2.0"]
                 [compojure "1.1.5" :exclusions [ring/ring-core]]
                 [com.keminglabs/jetty7-websockets-async "0.1.0"]
                 [prismatic/dommy "0.1.1"]]
  :uberjar-name "torus-pong.jar"
  :main torus-pong.main
  :hooks [leiningen.cljsbuild]
  :source-paths ["src/clj"
                 "src/cljx"]
  :plugins [[lein-cljsbuild "0.3.3"]]
  :cljsbuild {:crossovers [torus-pong.game.params]
              :builds
              [{;; :notify-command ["terminal-notifier" "-title" "lein-cljsbuild" "-message"]
                :source-paths ["src/cljs"]
                :crossover-path "src/cljx"
                :compiler {:libs ["src/js"]
                           :output-to "resources/public/pong.js"
                           :pretty-print true
                           :optimizations :whitespace}}]}
  :profiles {:dev
             {:source-paths ["dev"]
              :dependencies [[org.clojure/tools.namespace "0.2.3"]
                             [org.clojure/java.classpath "0.2.1"]]}})
