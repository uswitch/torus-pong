(ns torus-pong.game.core
  (:require [torus-pong.game.params :as params]))

(defn initial-field-state
  [id]
  {:player {:position (/ params/game-height 2)
            :id id}
   :balls [{:p {:x  (int (rand params/game-width))
                :y  (int (rand params/game-height))}
            :v  {:x 10 :y 10}}]})

(def initial-game-state
  {:fields []})

(defn find-field-index
  [game-state id]
  (some (fn [[idx v]] (if (= v id) idx))
        (map-indexed (fn [ix v] [ix (-> v :player :id)]) (:fields game-state))))

(defn move-up
  [position]
  (if (< (+ position (/ params/paddle-height 2)) params/game-height)
    (reduce min [(+ position (long params/distance-paddle-moves-per-tick)) params/game-height])
    position))

(defn move-down
  [position]
  (if (> (- position (/ params/paddle-height 2)) 0)
    (reduce max [(- position (long params/distance-paddle-moves-per-tick)) 0])
    position))

(defmulti handle-command
  (fn [game-state command]
    (first command)))

(defmethod handle-command :player/leave
  [game-state [command id]]
  (assoc game-state :fields
         (vec (remove #(= (-> % :player :id) id) (:fields game-state)))))

(defmethod handle-command :player/join
  [game-state [command id]]
  (update-in game-state [:fields] conj (initial-field-state id)))

(defmethod handle-command :player/up
  [game-state [command id]]
  (let [field-index (find-field-index game-state id)]
    (update-in game-state [:fields field-index :player :position] move-up)))

(defmethod handle-command :player/down
  [game-state [command id]]
  (let [field-index (find-field-index game-state id)]
    (update-in game-state [:fields field-index :player :position] move-down)))

(defn handle-commands
  [game-state commands]
  (reduce (fn [current-state command]
            (handle-command current-state command))
          game-state commands))

(defn player-collision?
  [ball player]
  (let [x-distance (Math/abs (- (/ params/game-width 2) (-> ball :p :x)))
        y-distance (Math/abs (- (player :position) (-> ball :p :y)))]
    (and (< x-distance (+ (/ params/paddle-width 2) params/ball-radius))
         (< y-distance (+ (/ params/paddle-height 2) params/ball-radius)))))

(defn invert-velocity
  [ball]
  (-> ball
      (update-in [:v :x] -)
      (update-in [:v :y] -)))

(defn player-collision
  [ball player]
  (if (player-collision? ball player)
    (invert-velocity ball)
    ball))

(defn wall-collision?
  [ball]
  (or (>= (+ params/ball-radius (-> ball :p :y))
          params/game-height)
      (<= (-> ball :p :y) params/ball-radius)))

(defn wall-collision
  [ball]
  (if (wall-collision? ball)
    (update-in ball [:v :y] -)
    ball))

(defn apply-velocity
  [ball]
  (let [current-x-pos (-> ball :p :x)
        current-y-pos (-> ball :p :y)
        current-x-vel (-> ball :v :x)
        current-y-vel (-> ball :v :y)]
    {:p {:x (+ current-x-pos current-x-vel)
         :y (+ current-y-pos current-y-vel)}
     :v {:x current-x-vel
         :y current-y-vel}}))

(defn advance-ball
  [player ball]
  (-> ball
      (player-collision player)
      wall-collision
      apply-velocity))

(defn advance-field
  [field]
  (update-in field [:balls] (partial mapv (partial advance-ball (:player field)))))

(defn advance-fields
  [game-state]
  (update-in game-state [:fields] (partial mapv advance-field)))

(defn advance
  "Given a game-state and some inputs, advance the game-state one
  tick"
  [game-state commands]
  (let [new-game-state (-> game-state
                           (handle-commands commands)
                           (advance-fields))]
    (println new-game-state)
    new-game-state))
