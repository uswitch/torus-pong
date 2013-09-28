(ns torus-pong.async.websocket
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [chan timeout put!]]
            [goog.events]
            [goog.json]
            [goog.string]
            [goog.net.WebSocket]
            [goog.net.WebSocket.MessageEvent]
            [goog.net.WebSocket.EventType :as Events]))


(defn connect!
  [url]
  (let [ws  (goog.net.WebSocket.)
        in  (chan)
        out (chan)]
    (goog.events.listen ws Events/OPENED (fn [e] (put! out [:opened e])))
    (goog.events.listen ws Events/CLOSED (fn [e] (put! out [:closed e])))
    (goog.events.listen ws Events/MESSAGE (fn [e] (put! out [:message (.-message e)])))
    (goog.events.listen ws Events/ERROR (fn [e] (put! out [:error e])))
    (.open ws url)
    (go (loop [msg (<! in)]
          (when msg
            (.send ws msg)
            (recur (<! in)))))
    {:in in :out out}))


