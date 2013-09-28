(ns user
  (:require [torus-pong.system :refer [init start! stop!]]))

(defonce system nil)

(defn go!
  "Start an instance of the system in the repl, and bind it to -system"
  []
  (when system
    (alter-var-root #'system stop!))
  (alter-var-root #'system (constantly (start! (init)))))
