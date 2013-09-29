(ns torus-pong.views.torus
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async
             :refer [chan sliding-buffer alts! >! <! timeout close!]]
            [torus-pong.utils :refer [log]]
            [torus-pong.game.params :as game-params]
            [goog.events]
            [goog.dom]))

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
  [offset s theta y]
  [(+ offset (* (canvas-y s y)
                (Math/cos (- theta half-pi))))
   (+ offset (* (canvas-y s y)
                (Math/sin (- theta half-pi))))])

(defn moveTo-on-circle
  [context offset s theta y]
  (let [[circle-x circle-y] (circle-pos offset s theta y)]
    (.moveTo context circle-x circle-y)))

(defn lineTo-on-circle
  [context offset s theta y]
  (let [[circle-x circle-y] (circle-pos offset s theta y)]
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

(defn draw-score
  [context offset s theta score name]
  (let [[x y] (circle-pos offset s theta (* 1.15 game-params/game-height))]
    (.save context)
    (.translate context x y)
    (.rotate context theta)
    (set! (.-font context) "16px Arial")
    (.fillText context name 0 0)
    (.restore context))
  (let [[x y] (circle-pos offset s theta (* -0.1 game-params/game-height))]
    (.save context)
    (.translate context x y)
    (.rotate context theta)
    (set! (.-font context) "bold 16px Arial")
    (.fillText context score 0 10)
    (.restore context)))

(defn draw-players
  [context offset s players]
  (let [n (count players)]
    (when (> n 0)
      (set! (.-strokeStyle context) "#fff")
      (set! (.-lineWidth context) 10)
      (set! (.-fillStyle context) "#fff")
      (set! (.-font context) "bold 16px Arial")
      (set! (.-textAlign context) "center")
      (set! (.-textBaseline context) "middle")
      (let [thetas (for [i (range n)] (* (/ i n) (* 2 Math/PI)))]
        (doseq [[theta player] (map vector thetas players)]
          (doto context
            (draw-score offset s theta (:score player) (:name player))
            (.beginPath)
            (moveTo-on-circle
             offset s theta (- (:position player) (/ game-params/paddle-height 2)))
            (lineTo-on-circle
             offset s theta (+ (:position player) (/ game-params/paddle-height 2)))
            (.stroke)))))))

(defn draw-last-winner
  [context offset last-winner]
  (when last-winner
    (set! (.-fillStyle context) "#fff")
    (set! (.-font context) "bold 16px Arial")
    (set! (.-textAlign context) "center")
    (set! (.-textBaseline context) "middle")
    (.fillText context "Last Winner:" offset (- offset 10))
    (.fillText context last-winner offset (+ offset 10))))

(defn draw-balls-in-field
  [context offset s n index field]
  (doseq [ball (:balls field)]
    (let [theta (+ (* index (/ (* 2 Math/PI) n))
                   (* (- (/ (-> ball :p :x) game-params/game-width) 0.5)
                      (/ (* 2 Math/PI) n)))
          [x y] (circle-pos offset s theta (-> ball :p :y))]
      (doto context
        (.beginPath)
        (moveTo-on-circle
         offset s theta (- (-> ball :p :y) game-params/ball-radius))
        (lineTo-on-circle
         offset s theta (+ (-> ball :p :y) game-params/ball-radius))
        (.stroke)))))

(defn draw-balls
  [context offset s fields]
  (let [n (count fields)]
    (when (> n 0)
      (set! (.-strokeStyle context) "#fff")
      (set! (.-lineWidth context) 10)
      (doall (map-indexed (partial draw-balls-in-field context offset s n) fields)))))

(def play-sounds (atom true))

(defn update-view
  [canvas game-state]
  (let [context (.getContext canvas "2d")
        w (.-width canvas)
        h (.-height canvas)
        offset (/ w 2)
        s (- w 60)
        fields (:fields game-state)
        players (map :player fields)]
    (doto context
      (.clearRect 0 0 w h)
      (draw-arena offset s)
      (draw-players offset s players)
      (draw-balls offset s fields)
      (draw-last-winner offset (:last-winner game-state)))
    (when (and (:play-sound game-state) @play-sounds)
      (.play (.getElementById js/document "boop")))
    (.log js/console (:new-winner game-state))
    (when (:new-winner game-state)
      (.log js/console "playing winner")
      (.play (.getElementById js/document "winner")))))

(defn create!
  []
  (let [canvas (.getElementById js/document "canvas")
        c (chan (sliding-buffer 1))]
    (goog.events/listen (goog.dom/getElement "mute") "click"
                      #(swap! play-sounds false?))
    (go (loop [game-state (<! c)]
          (update-view canvas game-state)
          (recur (<! c))))
    c))
