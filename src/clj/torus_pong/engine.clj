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

(defn index-of
  [pred coll]
  (some (fn [[i val]] (when (pred val) i))
        (map-indexed (fn [i val] [i val]) coll)))

(defn player-index
  [fields player-id]
  (index-of (fn [field] (= (-> field :player :id) player-id))
            fields))

(defn put-in-front
  "Puts the element with the index index in front of the coll when seen
  as a cyclical seq."
  [coll index]
  (concat
   (drop index coll)
   (take index coll)))

(defn in-player-order
  "Shuffles the fields around, so that the one with the
  player identified with player-id is the first. If player-id is not
  found, the original fields is returned."
  [fields player-id]
  (let [index (player-index fields player-id)]
    (if index
      (vec (put-in-front fields index))
      fields)))

(defn game-state-in-player-order
  [game-state player-id]
  (assoc game-state :fields (in-player-order (:fields game-state) player-id)))

(defn game-state-emitter
  [game-state-channel clients-atom]
  (go
   (loop [game-state (<! game-state-channel)]
     (when game-state
       (doseq [[player-id client-chan] @clients-atom]
         (>! client-chan (-> game-state
                             (game-state-in-player-order player-id)
                             pr-str)))
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
