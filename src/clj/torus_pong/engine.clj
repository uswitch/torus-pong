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
  {:player         player-field
   :left-opponent  left-field
   :right-opponent right-field})

(defn player-game-states
  "Given a game-state, return a player game state for each player."
  [game-state]
  (let [fields   (:fields game-state)
        nplayers (count fields)]
    (->> (apply concat (repeat fields))
         (partition 3 1)
         (take nplayers)
         (map player-game-state))))

(comment

  (player-game-states {:fields [{:player {:position 500, :id 1}}]})

  (player-game-states {:fields [{:player {:position 500, :id 1}}
                                {:player {:position 500, :id 2}}]} )



  (player-game-states  {:fields [{:player {:position 500, :id 1}}
                                 {:player {:position 500, :id 2}}
                                 {:player {:position 500, :id 3}}
                                 ;{:player {:position 500, :id 4}}
                                 ]})


  (player-game-states {:fields [{:player {:position 500, :id 6}, :balls [{:p {:x 866, :y 400}, :v {:x 1, :y 1}}]} {:player {:position 500, :id 8}, :balls [{:p {:x 179, :y 670}, :v {:x 1, :y 1}}]}]})


  (doseq [ps ]
    (println (-> ps :player :id)))
  )


(defn game-state-emitter
  [game-state-channel clients-atom]
  (go
   (loop [game-state (<! game-state-channel)]
     (when game-state
       (doseq [player-game-state (player-game-states game-state)]
         (let [player-id   (-> player-game-state :player :player :id)
               client-chan (get @clients-atom player-id)]
           (>! client-chan (pr-str player-game-state))))
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
