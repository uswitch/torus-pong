(ns torus-pong.async.utils
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [<! >! chan timeout put!]]))

(defn event-chan
  ([type f] (event-chan js/window type f))
  ([el type f] (event-chan (chan) el type f))
  ([c el type f]
     (.addEventListener el type #(when-let [msg (f %)]
                                   (put! c msg)))
     c))

