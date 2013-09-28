(ns torus-pong.game.core
  (:require [torus-pong.game.params :as params]))

(defn initial-field-state
  [id]
  {:player {:position (/ params/game-height 2) :id id}})

(def initial-game-state
  {:fields []})

(defn find-field-index
  [game-state id]
  (some (fn [[idx v]] (if (= v id) idx))
        (map-indexed (fn [ix v] [ix (-> v :player :id)]) (:fields game-state))))

(defn move-up
  []
  (fn [position]
    (if (< position params/game-height)
      (inc position)
      position)))

(defn move-down
  []
  (fn [position]
    (if (> position 0)
      (dec position)
      position)))

(defmulti handle-command
  (fn [game-state command]
    (first command)))

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

(defn advance
  "Given a game-state and some inputs, advance the game-state one
  tick"
  [game-state commands]
  (let [new-game-state (handle-commands game-state commands)]
    (println new-game-state)
    new-game-state))
