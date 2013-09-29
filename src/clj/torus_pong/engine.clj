(ns torus-pong.engine
  (:require [clojure.core.async :refer [go alts! <! >! timeout]]
            [torus-pong.game.params :as params]
            [torus-pong.game.core   :as game-core]))

(defn spawn-engine-process!
  [command-chan game-state-channel]
  (go (loop [game-state game-core/initial-game-state
             commands   []
             timer      (timeout (long params/tick-ms))]
        (let [[v c] (alts! [timer command-chan] :priority true)]
          (condp = c
            command-chan (when v
                           (recur game-state (conj commands v) timer))
            timer        (do (let [updated-game-state (game-core/advance game-state commands)]
                               (>! game-state-channel updated-game-state)
                               (recur updated-game-state [] (timeout (long params/tick-ms))))))))

      (println "Exiting engine process")))

;; game-state-emitter

(defn game-state-emitter
  [game-state-channel clients-atom]
  (go
   (loop [game-state (<! game-state-channel)]
     (when game-state
       (doseq [client-chan (vals @clients-atom)]
         (>! client-chan (pr-str game-state)))
       (recur (<! game-state-channel))))
   (println "Exiting game state emitter loop")))

(defn full-game-state-emitter
  [game-state-channel listen-clients-atom]
  (go
   (loop [game-state (<! game-state-channel)]
     (when game-state
       (doseq [client-chan (vals @listen-clients-atom)]
         (>! client-chan (pr-str game-state)))
       (recur (<! game-state-channel))))
   (println "Exiting full game state emitter loop")))
