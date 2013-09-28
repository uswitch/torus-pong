(ns torus-pong.engine
  (:require [clojure.core.async :refer [go alts! <! >! timeout]]
            [torus-pong.game.params :as params]))

(defn spawn-engine-process!
  [command-chan]
  (go (loop [game-state {}
             commands   []]
        (let [[v c] (alts! [command-chan ])]
          (condp = c
            command-chan (do (println "Got command" v)
                             (recur game-state commands)))))))
