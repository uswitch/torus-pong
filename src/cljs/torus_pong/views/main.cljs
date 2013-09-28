(ns torus-pong.views.main
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [chan sliding-buffer alts! >! <! timeout close!]]
            [torus-pong.utils :refer [log]]
            [torus-pong.game.params :as game-params]
            [visual.Visualiser]))


;;vis (visual.Visualiser. "canvas" game-params/game-height 100)

(defn create!
  []
  (let [c (chan (sliding-buffer 1))]
    (go (loop [player-game-state (<! c)]
          (log ["Got player-game-state" player-game-state])
          (recur (<! c))))
    c))





