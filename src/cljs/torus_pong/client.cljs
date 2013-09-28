(ns torus-pong.client
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [alts! >! <! timeout close!]]
            [torus-pong.async.websocket :as websocket]
            [torus-pong.async.utils :refer [event-chan map-chan]]
            [torus-pong.utils :refer [log]]))



(defn ws-test
  []
  (let [{:keys [in out]} (websocket/connect! "ws://localhost:8080")]
    (go (<! (timeout 100))
        (>! in "Hello!")
        (close! in))))

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
  (event-chan "keyup" key-event->command))


;; client process

(defn spawn-client-process!
  [command-chan]
  (go (while true
        (let [[v c] (alts! [command-chan])]
          (condp = c
            command-chan (do (log v)))))))

(defn ^:export run
  []
  (.log js/console "pong!")
  (spawn-client-process! (command-chan)))
