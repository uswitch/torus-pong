(ns torus-pong.game.params)
(def paddle-size 100)

(def ms-to-move-paddle-from-top-to-bottom 3000)

(def game-height 1000)

(def ticks-per-sec 1)

(def tick-ms (/ 1000 ticks-per-sec))

(def max-commands-per-tick 10)

(def distance-paddle-moves-per-tick (/ (- game-height paddle-size)
                                       (/ ms-to-move-paddle-from-top-to-bottom tick-ms)
                                        max-commands-per-tick))
