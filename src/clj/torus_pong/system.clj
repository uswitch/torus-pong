(ns torus-pong.system
  (:require [clojure.core.async :refer [chan]]
            [com.keminglabs.jetty7-websockets-async.core :as ws]
            [ring.adapter.jetty :refer [run-jetty]]
            [torus-pong.server :as server]
            [torus-pong.engine :as engine]))

(defn init
  []
  {
   ;; channel for new websocket connections
   :connection-chan (chan)

   ;; channel for all commands from clients
   :command-chan    (chan)

      ;; channel for communicating the game state
   :game-state-chan    (chan)})

(defn jetty-configurator
  [system]
  (ws/configurator (system :connection-chan)))

(defn start!
  [system]
  (println "Starting system")

  (engine/spawn-engine-process! (:command-chan system) (:game-state-chan system))
  (engine/game-state-emitter    (:game-state-chan system))
  (server/spawn-connection-process! (:connection-chan system)
                                    (:command-chan system))

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
