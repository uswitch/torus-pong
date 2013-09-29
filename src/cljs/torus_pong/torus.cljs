(ns torus-pong.torus
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [alts! >! <! timeout close!]]
            [cljs.reader :as reader]
            [torus-pong.utils :refer [log host]]
            [torus-pong.async.websocket :as websocket]
            [torus-pong.async.utils :refer [event-chan map-chan]]
            [torus-pong.views.torus]))

(defn spawn-client-process!
  [ws-in ws-out torus-view]
  (go
   (while true
     (let [[type data] (<! ws-out)]
       (case type
         :message (let [game-state (reader/read-string data)]
                    (>! torus-view game-state))
         (log ["Silently ignoring" [type data]]))))))

(defn ^:export run
  []
  (.log js/console "The entire world!")
  (let [torus-view (torus-pong.views.torus/create!)
        {:keys [in out]} (websocket/connect! (str "ws://" host "/just-listen"))]
    (spawn-client-process! in out torus-view)))
