(ns torus-pong.game.core
  (:require [torus-pong.game.params :as params]))

(def initial-game-state
  {:position (/ params/game-height 2)})

(defn move-players
  [game-state commands]
  (reduce
   (fn [game-state [command]]
     (println "Command to process: " command)
     (let [movement-fn (if (= :player/up command) inc dec)]
       (update-in game-state [:position] movement-fn)))
   game-state
   commands))

(defn advance
  "Given a game-state and some inputs, advance the game-state one
  tick"
  [game-state commands]
  (let [new-game-state (-> game-state
                           (move-players commands))]
    (println new-game-state)
    new-game-state))
