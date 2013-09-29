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
  [context s players]
  (let [n (count players)]
    (when (> n 0)
      (set! (.-strokeStyle context) "#fff")
      (set! (.-lineWidth context) 10)
      (let [thetas (for [i (range n)] (* (/ i n) (* 2 Math/PI)))]
        (doseq [[theta player] (map vector thetas players)]
          (doto context
            (.beginPath)
            (moveTo-on-circle s theta (- (:position player) game-params/paddle-height))
            (lineTo-on-circle s theta (+ (:position player) game-params/paddle-height))
            (.stroke)))))))

(defn draw-balls-in-field
  [context s n index field]
  (doseq [ball (:balls field)]
    (let [theta (+ (* index (/ (* 2 Math/PI) n))
                   (* (- (/ (-> ball :p :x) game-params/game-width) 0.5)
                      (/ (* 2 Math/PI) n)))
          [x y] (circle-pos s theta (-> ball :p :y))]
      (doto context
        (.fillRect (- x 5) (- y 5) 10 10)))))

(defn draw-balls
  [context s fields]
  (let [n (count fields)]
    (when (> n 0)
      (set! (.-fillStyle context) "#fff")
      (doall (map-indexed (partial draw-balls-in-field context s n) fields)))))

(defn update-view
  [canvas game-state]
  (let [context (.getContext canvas "2d")
        w (.-width canvas)
        h (.-height canvas)
        s w
        fields (:fields game-state)
        players (map :player fields)]
    (doto context
      (.clearRect 0 0 w h)
      (draw-arena s)
      (draw-players s players)
      (draw-balls s fields))

    ))

(defn create!
  []
  (let [canvas (.getElementById js/document "canvas")
        c (chan (sliding-buffer 1))]
    (go (loop [game-state (<! c)]
          (update-view canvas game-state)
          (recur (<! c))))
    c))
