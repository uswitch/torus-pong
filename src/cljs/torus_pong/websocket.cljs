(ns torus-pong.websocket
  (:require [cljs.core.async :refer [chan timeout put]]
            [goog.events]
            [goog.json]
            [goog.string]
            [goog.net.WebSocket]
            [goog.net.WebSocket.MessageEvent]
            [goog.net.WebSocket.EventType :as Events]))


(defn websocket-chan
  [url]
  (let [ws (goog.net.WebSocket.)
        c  (chan)]
    (goog.events.listen ws Events/OPENED (fn [e] (put! c [:opened e])))
    (goog.events.listen ws Events/CLOSED (fn [e] (put! c [:closed e])))
    (goog.events.listen ws Events/MESSAGE (fn [e] (put! c [:message (.-message e)])))
    (goog.events.listen ws Events/ERROR (fn [e] (put! c [:error e])))
    (.open ws url)
    c))


