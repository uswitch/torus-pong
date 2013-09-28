(ns torus-pong.server
  (:require [com.keminglabs.jetty7-websockets-async.core :as ws]
            [clojure.core.async :refer [go <! >!]]
            [compojure.core :refer [routes]]
            [compojure.route :as route]
            [clojure.edn     :as edn])
  (:import  [java.util.concurrent.atomic AtomicLong]))

(def handler
  (routes (route/files "/" {:root "resources/public"})))


(def id
  (java.util.concurrent.atomic.AtomicLong.))

(defn next-id
  []
  (.incrementAndGet id))


(defn spawn-client-process!
  [ws-request ws-in ws-out command-chan]
  (go
   (loop [msg (<! ws-out)]
        (when msg
          (let [command (edn/read-string msg)]
            (println "Got message from client: " command)
            (>! command-chan command)
            (recur (<! ws-out)))))
      (println "Client process terminating")))

(defn spawn-connection-process!
  [conn-chan command-chan clients]
  (go (loop [{:keys [request in out] :as conn} (<! conn-chan)]
        (when conn
          (let [id (next-id)]
            (println "Spawning new client process for" (:remote-addr request))
            (swap! clients assoc id in)
            (println @clients)
            (spawn-client-process! request in out command-chan)
            (recur (<! conn-chan)))))
      (println "Connection process terminating")))
