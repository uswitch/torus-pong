(ns torus-pong.views.main
  (:require-macros [dommy.macros :refer [deftemplate node sel sel1]]
                   [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [chan sliding-buffer alts! >! <! timeout close!]]
            [clojure.set :as set]
            [dommy.core :as dom]
            [torus-pong.utils :refer [log]]
            [torus-pong.async.utils :refer [event-chan]]
            [torus-pong.game.params :as game-params]))


(defn make-view
  []
  (let [player         (node [:div.player.paddle])
        left-opponent  (node [:div.left-opponent.paddle])
        right-opponent (node [:div.right-opponent.paddle])
        main-view      (node [:div.main-view player left-opponent right-opponent])]
    (dom/append! (sel1 "#container") main-view)
    {:main-view main-view
     :player player
     :left-opponent left-opponent
     :right-opponent right-opponent}))

;;
;; scaling 
;;

(defn view-scale
  [view-width view-height]
  {:view-width  view-width
   :view-height view-height
   :x-scale     (/ view-width (+ (* game-params/game-width 2)
                                 game-params/paddle-width))
   :y-scale     (/ view-height game-params/game-height)})

(defn px-width
  [scale game-w]
  (* (:x-scale scale) game-w))

(defn px-height
  [scale game-h]
  (* (:y-scale scale) game-h))

(defn left-offset
  [scale game-x]
  (* (:x-scale scale) game-x))

(defn top-offset
  [scale game-y]
  (- (:view-height scale)
     (* (:y-scale scale) game-y)))


;;

(defn position-paddle!
  [el scale game-x game-y]
  (let [paddle-height      (px-height scale game-params/paddle-height)
        paddle-width       (px-width scale game-params/paddle-width)
        paddle-top-offset  (- (top-offset scale game-y) (/ paddle-height 2))
        paddle-left-offset (left-offset scale game-x)]
    (-> el
        (dom/set-px! :top paddle-top-offset
                     :left paddle-left-offset
                     :width paddle-width
                     :height paddle-height))))

;; move existing balls
;; add new balls
;; remove removed balls

(defn move-ball!
  [el ball]
  (dom/set-px! el
               :top    (- (- 300 (* 0.3 (-> ball :p :y))) (/ (* 0.3 game-params/ball-radius) 2))
               :left   (* 0.3 (-> ball :p :x))
               :width  (* 0.3 game-params/ball-radius)
               :height (* 0.3 game-params/ball-radius)))



(defn update-view
  [view player-game-state]
  (let [{:keys [player left-opponent right-opponent]} player-game-state]
    (let [scale (view-scale 600 300)]
      (position-paddle! (:left-opponent view)
                        scale
                        0
                        (-> left-opponent :player :position))
      (position-paddle! (:player view)
                        scale
                        game-params/game-width
                        (-> player :player :position))
      (position-paddle! (:right-opponent view)
                        scale
                        (* 2 game-params/game-width)
                        (-> right-opponent :player :position)))
    ))


(defn create!
  []
  (let [c (chan (sliding-buffer 1))
        view (make-view)]
    (go (loop [player-game-state (<! c)]
                                        ;(log ["Got player-game-state" player-game-state])
          (update-view view player-game-state)
          (recur (<! c))))
    c))





