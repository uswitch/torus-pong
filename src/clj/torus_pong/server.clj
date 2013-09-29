(ns torus-pong.server
  (:require [clojure.core.async :refer [go close! <! >! sliding-buffer chan]]
            [clojure.edn     :as edn]
            [com.keminglabs.jetty7-websockets-async.core :as ws]
            [compojure.core :refer [routes]]
            [compojure.route :as route]
            [torus-pong.async :refer [forward!]]
            [torus-pong.engine :as engine]
            [torus-pong.game.names :as names])
  (:import  [java.util.concurrent.atomic AtomicLong]))

(def handler
  (routes (route/files "/" {:root "resources/public"})))

(def next-id!
  (let [counter (java.util.concurrent.atomic.AtomicLong.)]
    (fn [] (.incrementAndGet counter))))

(defn spawn-client-process!
  [ws-in ws-out command-chan id clients name]
  (let [in (chan (sliding-buffer 1))]
    (swap! clients assoc id in)
    (forward! in ws-in)
    (go
     (>! command-chan [:player/join id name])
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
  [game id in out name]
  (println "Client joined " id " name: " name)
  (spawn-client-process! in out (:command-chan game) id (:clients game) name))

(defn stop-game!
  [game]
  (println "Stopping game")
  (close! (:command-chan game))
  (close! (:game-state-chan game)))

(defn game-status
  [game]
  (if (empty? @(:clients game))
    :empty
    :active))

(defn prune-empty-games!
  [games]
  (println "Pruning empty games")
  (let [games-by-status (group-by game-status games)]
    (clojure.pprint/pprint games-by-status)
    (doseq [game (:empty games-by-status)]
      (stop-game! game))
    (vec (:active games-by-status))))
;;
;; connections
;;

(defn spawn-connection-process!
  [conn-chan]
  (go (loop [games []]
        #_(clojure.pprint/pprint games)
        (let [{:keys [request in out] :as conn} (<! conn-chan)]
          (when conn
            (let [id (next-id!)
                  name (names/next-name)]
              (if-let [game (find-available-game games)]
                (do
                  (join-game! game id in out name)
                  (recur (prune-empty-games! games)))
                (do
                  (let [game (start-new-game!)]
                    (join-game! game id in out name)
                    (recur (conj (prune-empty-games! games) game)))))))))
      (println "Connection process terminating")))
