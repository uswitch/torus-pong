(ns torus-pong.server
  (:require [com.keminglabs.jetty7-websockets-async.core :as ws]
            [clojure.core.async :refer [go <! >!]]
            [compojure.core :refer [routes]]
            [compojure.route :as route]))

(def handler
  (routes (route/files "/")))

(defn spawn-client-process!
  [request in out]
  (go (loop [msg (<! out)]
        (when msg
          (println "Got message from client: " msg)
          (recur (<! out))))
      (println "Client process terminating")))

(defn spawn-connection-process!
  [conn-chan]
  (go (loop [{:keys [request in out] :as conn} (<! conn-chan)]
        (when conn
          (println "Spawning new client process for" (:remote-addr request))
          (spawn-client-process! request in out)
          (recur (<! conn-chan))))
      (println "Connection process terminating")))
