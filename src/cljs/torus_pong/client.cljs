(ns torus-pong.client
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [alts! >! <! timeout close!]]
            [cljs.reader :as reader]
            [torus-pong.utils :refer [log host]]
            [torus-pong.async.websocket :as websocket]
            [torus-pong.async.utils :refer [event-chan map-chan]]
            [torus-pong.views.torus]))

;; commands

(defn key-event->command
  [e]
  (let [code (.-keyCode e)]
    (case code
      38 [:player/up]
      40 [:player/down]
      87 [:player/up]
      83 [:player/down]
      nil)))

(defn command-chan
  []
  (event-chan "keydown" key-event->command))

;; client process

(defn spawn-client-process!
  [ws-in ws-out command-chan torus-view]
  (go (while true
        (let [[v c] (alts! [ws-out command-chan])]
          (condp = c

            ws-out
            (do
              (let [[type data] v]
                (case type
                  :message (let [game-state (reader/read-string data)]
                             (>! torus-view game-state))

                  (log ["Silently ignoring" v]))))

            command-chan
            (>! ws-in v))))))

(defn ^:export run
  []
  (.log js/console "pong!")
  (let [torus-view (torus-pong.views.torus/create!)
        {:keys [in out]} (websocket/connect! (str "ws://" host))]
    (spawn-client-process! in out (command-chan) torus-view)))
