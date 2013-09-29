(ns torus-pong.async
  (:require [clojure.core.async :refer [go <! >! sliding-buffer chan]]))

(defn forward!
  [from to]
  (go
   (loop [msg (<! from)]
     (>! to msg)
     (when msg
       (recur (<! from))))))
