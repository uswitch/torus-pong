(ns torus-pong.utils)

(defn log
  [obj]
  (.log js/console (pr-str obj)))

(def host
  (aget js/window "location" "host"))
