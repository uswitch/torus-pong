(ns torus-pong.views.torus
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [chan sliding-buffer alts! >! <! timeout close!]]
            [torus-pong.utils :refer [log]]
            [torus-pong.async.utils :refer [event-chan]]
            [torus-pong.game.params :as game-params]))

(defn draw-arena
  [context s]
  (set! (.-strokeStyle context) "#fff")
  (set! (.-lineWidth context) 10)
  (doto context
    (.beginPath)
    (.arc (/ s 2) (/ s 2)
          (/ s 2)
          0 (* 2 Math.PI))
    (.stroke)
    (.beginPath)
    (.arc (/ s 2) (/ s 2)
          (/ s 4)
          0 (* 2 Math.PI))
    (.stroke)))

(defn circle-pos
  [s theta y]
  [(+ (/ s 2) (* (+ (/ y game-params/game-height) 1)
                 (/ s 4)
                 (Math/cos theta)))
   (+ (/ s 2) (* (+ (/ y game-params/game-height) 1)
                 (/ s 4)
                 (Math/sin theta)))])

(defn moveTo-on-circle
  [context s theta y]
  (let [[circle-x circle-y] (circle-pos s theta y)]
    (.moveTo context circle-x circle-y)))

(defn lineTo-on-circle
  [context s theta y]
  (let [[circle-x circle-y] (circle-pos s theta y)]
    (.lineTo context circle-x circle-y)))

(defn draw-players
  [context players s]
  (let [n (count players)]
    (when (> n 0)
      (set! (.-strokeStyle context) "#fff")
      (set! (.-lineWidth context) 10)
      (let [thetas (for [i (range n)] (+ (/ Math/PI 2) (* (/ i n) (* 2 Math/PI))))]
        (doseq [[theta player] (map vector thetas players)]
          (doto context
            (.beginPath)
            (moveTo-on-circle s theta (- (:position player) game-params/paddle-height))
            (lineTo-on-circle s theta (+ (:position player) game-params/paddle-height))
            (.stroke)))))))

(defn update-view
  [canvas game-state]
  (let [context (.getContext canvas "2d")
        w (.-width canvas)
        h (.-height canvas)
        s w
        players (map :player (:fields game-state))]
    (doto context
      (.clearRect 0 0 w h)
      (draw-arena s)
      (draw-players players s))

    ))

(defn create!
  []
  (.log js/console (Math/cos Math/PI))
  (let [canvas (.getElementById js/document "canvas")
        c (chan (sliding-buffer 1))]
    (go (loop [game-state (<! c)]
          (update-view canvas game-state)
          (recur (<! c))))
    c))
