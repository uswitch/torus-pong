(ns torus-pong.system
  (:require [clojure.core.async :refer [chan]]
            [com.keminglabs.jetty7-websockets-async.core :as ws]
            [ring.adapter.jetty :refer [run-jetty]]
            [torus-pong.server :as server]))

(defn init
  []
  {:connection-chan (chan)})

(defn jetty-configurator
  [system]
  (ws/configurator (system :connection-chan)))


(defn start!
  [system]
  (println "Starting system")
  
  (server/spawn-connection-process! (system :connection-chan))
  
  (assoc system
    :server (run-jetty server/handler
                       {:join? false
                        :port 8080
                        :configurator (jetty-configurator system)})))


(defn stop!
  [system]
  (println "Stopping system")
  (when-let [server (:server system)]
    (.stop server))

  (dissoc system :server))

