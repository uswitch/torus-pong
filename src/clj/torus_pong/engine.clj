(ns torus-pong.engine
  (:require [clojure.core.async :refer [go alts! <! >! timeout]]
            [torus-pong.game.params :as params]
            [torus-pong.game.core   :as game-core]))

(defn spawn-engine-process!
  [command-chan game-state-channel]
  (go (loop [game-state game-core/initial-game-state
             commands   []
             timer      (timeout params/tick-ms)]
        (let [[v c] (alts! [timer command-chan] :priority true)]
          (condp = c
            command-chan (do (println "Got command " v)
                             (when v
                               (recur game-state (conj commands v) timer)))
            timer        (do (println "TIMEOUT!" commands)
                             (let [updated-game-state (game-core/advance game-state commands)]
                               (>! game-state-channel updated-game-state)
                               (recur updated-game-state [] (timeout params/tick-ms)))))))

      (println "Exiting engine process")


      ))

(defn game-state-emitter
  [game-state-channel]
  (go
   (loop [game-state (<! game-state-channel)]
     (println "Reading game state")
     (when game-state
       (recur (<! game-state-channel))))
   (println "Exiting game state emitter loop")))
