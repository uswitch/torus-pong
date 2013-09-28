(ns torus-pong.async.utils
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [<! >! chan timeout put!]]))

(defn event-chan
  "Given an event type, and a function, return a channel where the
  result of applying f to every event of the given type is put when
  the event occurs. Optionally specify an element to limit event
  scope, and an existing channel, where output should be put."
  ([type f] (event-chan js/window type f))
  ([el type f] (event-chan (chan) el type f))
  ([c el type f]
     (.addEventListener el type #(when-let [msg (f %)]
                                   (put! c msg)))
     c))

