(ns torus-pong.client
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [alts! >! <! timeout close! chan]]
            [cljs.reader :as reader]
            [torus-pong.utils :refer [log host]]
            [torus-pong.async.websocket :as websocket]
            [torus-pong.async.utils :refer [event-chan map-chan]]
            [torus-pong.views.torus]
            [goog.events]
            [goog.dom]))

;; keys

(def key-down (atom nil))

(defn key-event->command
  [e]
  (let [code (.-keyCode e)]
    (case code
      38 :up
      40 :down
      87 :up
      83 :down
      nil)))

(defn bind-key-observer
  [command-chan]
  (go (while true
        (<! (timeout 50))
        (case @key-down
          :up   (>! command-chan [:player/up])
          :down (>! command-chan [:player/down])
          :not-matched)))
  (.addEventListener js/window "keydown"
                     (fn [e]
                       (.log js/console e)
                       (reset! key-down (key-event->command e))))
  (.addEventListener js/window "keyup"
                     (fn [e]
                       (reset! key-down nil))))

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

(def clicked (atom nil))

(defn bind-arrow-click
  [command-chan]
  (go (while true
        (<! (timeout 50))
        (case @clicked
          :up   (>! command-chan [:player/up])
          :down (>! command-chan [:player/down])
          :not-matched)))
  (goog.events/listen (goog.dom/getElement "up-arrow") "mouseover"
                      #(reset! clicked :up))
  (goog.events/listen (goog.dom/getElement "up-arrow") "mouseout"
                      #(reset! clicked nil))
  (goog.events/listen (goog.dom/getElement "up-arrow") "touchstart"
                      #(reset! clicked :up))
  (goog.events/listen (goog.dom/getElement "up-arrow") "touchend"
                      #(reset! clicked nil))

  (goog.events/listen (goog.dom/getElement "down-arrow") "mouseover"
                      #(reset! clicked :down))
  (goog.events/listen (goog.dom/getElement "down-arrow") "mouseout"
                      #(reset! clicked nil))
  (goog.events/listen (goog.dom/getElement "down-arrow") "touchstart"
                      #(reset! clicked :down))
  (goog.events/listen (goog.dom/getElement "down-arrow") "touchend"
                      #(reset! clicked nil)))

(defn ^:export run
  []
  (.log js/console "pong!")
  (let [torus-view (torus-pong.views.torus/create!)
        {:keys [in out]} (websocket/connect! (str "ws://" host))
        command-channel (chan)]
    (spawn-client-process! in out command-channel torus-view)
    (bind-key-observer command-channel)
    (bind-arrow-click command-channel)))
