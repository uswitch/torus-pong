(ns torus-pong.async.utils
  ;;(:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :refer [chan timeout put!]]))

(defn event-chan
  ([type] (event-chan js/window type))
  ([el type] (event-chan (chan) el type))
  ([c el type]
     (.addEventListener el type #(put! c %))
     c))
