(ns torus-pong.game.params)

(def paddle-height 300)
(def paddle-width 25)
(def ball-radius 25)
(def ball-speed 30)
(def scoring :hits)

(def winning-score 20)

(def ms-to-move-paddle-from-top-to-bottom 3000)

(def game-height 1000)
(def game-width  1000)

(def ticks-per-sec 20)

(def tick-ms (/ 1000 ticks-per-sec))

(def max-commands-per-tick 10)

(def distance-paddle-moves-per-tick 40 #_(/ (- game-height paddle-height)
                                       (/ ms-to-move-paddle-from-top-to-bottom tick-ms)
                                        max-commands-per-tick))
