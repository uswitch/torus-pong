(ns torus-pong.client
;  (:require [torus-pong.websocket :refer [websocket-chan]])
  )



(defn ^:export run
  []
  (.log js/console "pong!"))
