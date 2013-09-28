(ns torus-pong.client
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [alts! >! <! timeout close!]]
            [torus-pong.async.websocket :refer [websocket-chan]]))



(defn ws-test
  []
  (let [{:keys [in out]} (websocket-chan "ws://localhost:8080")]
    (go (<! (timeout 100))
        (>! in "Hello!")
        (close! in))))


(defn ^:export run
  []
  (.log js/console "pong!")
  (ws-test)
  )
