(ns torus-pong.views.torus
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async
             :refer [chan sliding-buffer alts! >! <! timeout close!]]
            [torus-pong.utils :refer [log]]
            [torus-pong.game.params :as game-params]))

;; constants

(def pi
  Math/PI)

(def half-pi
  (/ Math/PI 2))

(def two-pi
  (* Math/PI 2))

;; translation helpers

(defn canvas-y
  [s game-y]
  (* (+ (/ game-y game-params/game-height) 1)
     (/ s 4)))

(defn circle-pos
  [s theta y]
  [(+ (/ s 2) (* (canvas-y s y)
                 (Math/cos (- theta half-pi))))
   (+ (/ s 2) (* (canvas-y s y)
                 (Math/sin (- theta half-pi))))])

(defn moveTo-on-circle
  [context s theta y]
  (let [[circle-x circle-y] (circle-pos s theta y)]
    (.moveTo context circle-x circle-y)))

(defn lineTo-on-circle
  [context s theta y]
  (let [[circle-x circle-y] (circle-pos s theta y)]
    (.lineTo context circle-x circle-y)))

;; drawing

(defn draw-arena
  [context offset s]
  (set! (.-strokeStyle context) "#fff")
  (set! (.-lineWidth context) 10)
  (let [inner-radius (- (canvas-y s 0) 5)
        outer-radius (+ (canvas-y s game-params/game-height) 5)]
    (doto context
      (.beginPath)
      (.arc offset offset inner-radius 0 two-pi)
      (.stroke)
      (.beginPath)
      (.arc offset offset outer-radius 0 two-pi)
      (.stroke))))

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
            (moveTo-on-circle
             s theta (- (:position player) game-params/paddle-height))
            (lineTo-on-circle
             s theta (+ (:position player) game-params/paddle-height))
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
        offset (/ w 2)
        s (- w 20)
        fields (:fields game-state)
        players (map :player fields)]
    (doto context
      (.clearRect 0 0 w h)
      (draw-arena offset s)
      (draw-players s players)
      (draw-balls s fields))))

(defn create!
  []
  (let [canvas (.getElementById js/document "canvas")
        c (chan (sliding-buffer 1))]
    (go (loop [game-state (<! c)]
          (update-view canvas game-state)
          (recur (<! c))))
    c))
