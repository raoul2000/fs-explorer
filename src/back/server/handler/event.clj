(ns server.handler.event
  (:require [clojure.core.async :as async]
            [clojure.core.async.impl.protocols :as chan]))


#_(defn create [{:keys [polite?] :as options}]
  (fn [request]
    (let [name (get-in request [:params :name])]
      (response/ok {:response (say-hello name polite?)}))))

(defn event-stream [event-chan context]
  (dotimes [i 10]
    (when-not (chan/closed? event-chan)
      (async/>!! event-chan {:name "counter" :data i})
      (Thread/sleep 1000)))
  (async/close! event-chan))