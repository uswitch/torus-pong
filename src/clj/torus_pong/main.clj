(ns torus-pong.main
  (:require [torus-pong.system :as system]))

(defn -main
  [& args]
  (system/start! (system/init)))
