(ns torus-pong.client
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [alts! >! <! timeout close!]]
            [cljs.reader :as reader]
            [torus-pong.async.websocket :as websocket]
            [torus-pong.async.utils :refer [event-chan map-chan]]
            [torus-pong.utils :refer [log]]
            [visual.Visualiser]))

;; commands

(defn key-event->command
  [e]
  (let [code (.-keyCode e)]
    (case code
      38 [:player/up]
      40 [:player/down]
      nil)))

(defn command-chan
  []
  (event-chan "keydown" key-event->command))

;; client process

(defn spawn-client-process!
  [ws-in ws-out command-chan vis]
  (go (while true
        (let [[v c] (alts! [ws-out command-chan])]
          (condp = c

            ws-out
            (let [[type data] v]
              (case type
                :message (let [game-state [(reader/read-string data)]]
                           (.update vis (clj->js game-state)))

                (log ["Silently ignoring"])))

            command-chan
            (>! ws-in v))))))

(def host
  (aget js/window "location" "host"))

(defn ^:export run
  []
  (.log js/console "pong!")
  (let [vis (visual.Visualiser. "canvas")
        {:keys [in out]} (websocket/connect! (str "ws://" host))]
    (spawn-client-process! in out (command-chan) vis)))
