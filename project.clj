(defproject torus-pong "0.1.0-SNAPSHOT"
  :description "A multiplayer take on the classic Game of Pong. Entry for Clojure Cup 2013."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1909"]
                 [core.async "0.1.0-SNAPSHOT"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [ring/ring-servlet "1.2.0"]
                 [compojure "1.1.5" :exclusions [ring/ring-core]]
                 [org.clojure/core.match "0.2.0-rc5"]
                 [com.keminglabs/jetty7-websockets-async "0.1.0"]]
  )
