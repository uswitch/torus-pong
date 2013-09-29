(ns torus-pong.game.params)

(def paddle-height 100)
(def paddle-width 20)
(def ball-radius 10)
(def ball-speed 30)

(def ms-to-move-paddle-from-top-to-bottom 3000)

(def game-height 1000)
(def game-width  1000)

(def ticks-per-sec 20)

(def tick-ms (/ 1000 ticks-per-sec))

(def max-commands-per-tick 10)

(def distance-paddle-moves-per-tick 20 #_(/ (- game-height paddle-height)
                                       (/ ms-to-move-paddle-from-top-to-bottom tick-ms)
                                        max-commands-per-tick))
