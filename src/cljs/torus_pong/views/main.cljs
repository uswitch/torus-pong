(ns torus-pong.views.main
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [chan sliding-buffer alts! >! <! timeout close!]]
            [torus-pong.utils :refer [log]]
            [torus-pong.async.utils :refer [event-chan]]
            [torus-pong.game.params :as game-params]
            [visual.Visualiser]))





(defn update-view
  [v player-game-state]
  (let [{:keys [player left-opponent right-opponent]} player-game-state]
    (doto v
      (.clear)
      (.drawPlayer        (-> player :player :position))
      (.drawLeftOpponent  (-> left-opponent :player :position))
      (.drawRightOpponent (-> right-opponent :player :position)))
    (doseq [ball (-> player :balls)]
      (let [{:keys [x y]} (:p ball)]
        (.drawBall v x y)))))


(defn create!
  []
  (let [c (chan (sliding-buffer 1))
        v (visual.Visualiser. "canvas"
                              game-params/game-height
                              game-params/game-width
                              game-params/paddle-height
                              game-params/paddle-width)]
    (go (loop [player-game-state (<! c)]
          (log ["Got player-game-state" player-game-state])
          (update-view v player-game-state)
          (recur (<! c))))
    c))





