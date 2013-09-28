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

      (println "Exiting engine process")))

;; game-state-emitter

(comment
  ;; transformation of game-state to player-game-state
  
  {:fields [{:player {:position 500, :id 1}}
            {:player {:position 500, :id 2}}]}

  ;; =>
  
  {:player {:position 500, :id 2},
   :left-opponent {:position 500, :id 1},
   :right-opponent {:position 500, :id 1}} 

  )


(defn player-game-state
  [[left-field player-field right-field]]
  {:player (:player player-field)
   :left-opponent (:player left-field)
   :right-opponent (:player right-field)})

(defn player-game-states
  "Given a game-state, return a player game state for each player."
  [game-state]
  (let [fields   (:fields game-state)
        nplayers (count fields)]
    (->> (apply concat (repeat fields))
         (partition 3 nplayers )
         (take nplayers)
         (map player-game-state)
         )))

(comment

  (player-game-states {:fields [{:player {:position 500, :id 1}}]})

  (player-game-states {:fields [{:player {:position 500, :id 1}}
                                {:player {:position 500, :id 2}}]} )



  (player-game-states  {:fields [{:player {:position 500, :id 1}}
                                 {:player {:position 500, :id 2}}
                                 {:player {:position 500, :id 3}}
                                 {:player {:position 500, :id 4}}]})


  )


(defn game-state-emitter
  [game-state-channel clients-atom]
  (go
   (loop [game-state (<! game-state-channel)]
     (when game-state
       (println game-state)
       (doseq [player-game-state (player-game-states game-state)]
         (let [player-id   (-> player-game-state :player :id)
               client-chan (get @clients-atom player-id)]
           (>! client-chan (pr-str player-game-state))))
       (recur (<! game-state-channel))))
   (println "Exiting game state emitter loop")))
