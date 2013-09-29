(ns torus-pong.server
  (:require [clojure.core.async :refer [go <! >! sliding-buffer chan]]
            [clojure.edn     :as edn]
            [com.keminglabs.jetty7-websockets-async.core :as ws]
            [compojure.core :refer [routes]]
            [compojure.route :as route]
            [torus-pong.async :refer [forward!]]
            [torus-pong.engine :as engine])
  (:import  [java.util.concurrent.atomic AtomicLong]))

(def handler
  (routes (route/files "/" {:root "resources/public"})))

(def next-id!
  (let [counter (java.util.concurrent.atomic.AtomicLong.)]
    (fn [] (.incrementAndGet counter))))

(defn spawn-client-process!
  [ws-in ws-out command-chan id clients]
  (let [in (chan (sliding-buffer 1))]
    (swap! clients assoc id in)
    (forward! in ws-in)
    (go
     (>! command-chan [:player/join id])
     (loop [msg (<! ws-out)]
       (if msg
         (let [command (edn/read-string msg)]
           (>! command-chan (conj command id))
           (recur (<! ws-out)))
         (do (>! command-chan [:player/leave id])
             (swap! clients dissoc id))))
     (println "Client process terminating"))))

;;
;; games
;;

(defn find-available-game
  [games]
  (when-let [game (last games)]
    (when (< (count @(:clients game)) 3)
      game)))

(defn start-new-game!
  []
  (println "Starting new game")
  (let [command-chan     (chan)
        game-state-chan  (chan)
        clients          (atom {})]
    {:engine-process     (engine/spawn-engine-process! command-chan
                                                   game-state-chan)
     :game-state-emitter (engine/game-state-emitter game-state-chan
                                                    clients)
     :command-chan       command-chan
     :game-state-chan    game-state-chan
     :clients            clients}))

(defn join-game!
  [game id in out]
  (println "Client joined" id)
  (spawn-client-process! in out (:command-chan game) id (:clients game)))



;;
;; connections
;; 

(defn spawn-connection-process!
  [conn-chan]
  (go (loop [games []]
        #_(clojure.pprint/pprint games)
        (let [{:keys [request in out] :as conn} (<! conn-chan)]
          (when conn
            (let [id (next-id!)]
              (if-let [game (find-available-game games)]
                (do
                  (join-game! game id in out)
                  (recur games))
                (do
                  (let [game (start-new-game!)]
                    (join-game! game id in out)
                    (recur (conj games game)))))))))
      (println "Connection process terminating")))
