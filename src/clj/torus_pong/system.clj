(ns torus-pong.system
  (:require [clojure.core.async :refer [chan close!]]
            [clojure.core.async.lab :refer [broadcast]]
            [com.keminglabs.jetty7-websockets-async.core :as ws]
            [ring.adapter.jetty :refer [run-jetty]]
            [torus-pong.async :refer [forward!]]
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
   :game-state-chan (chan)

   :clients         (atom {})
   :listen-clients  (atom {})})

(defn jetty-configurator
  [system]
  (ws/configurator (system :connection-chan)))

(defn start!
  [system]
  (println "Starting system")

  (let [individual-game-state-chan (chan)
        full-game-state-chan (chan)
        game-state-chan (broadcast individual-game-state-chan
                                   full-game-state-chan)]
    (forward! (:game-state-chan system) game-state-chan)
    (engine/spawn-engine-process! (:command-chan system) (:game-state-chan system))
    (engine/game-state-emitter      individual-game-state-chan (:clients system))
    (engine/full-game-state-emitter full-game-state-chan (:listen-clients system)))
  (server/spawn-connection-process! (:connection-chan system)
                                    (:command-chan    system)
                                    (:clients         system)
                                    (:listen-clients  system))

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
  (close! (:command-chan system))
  (close! (:game-state-chan system))
  (close! (:connection-chan system))

  (dissoc system :server))
