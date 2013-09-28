(ns torus-pong.main
  (:require [torus-pong.system :as system])
  (:gen-class))

(defn -main
  [& args]
  (system/start! (system/init)))
