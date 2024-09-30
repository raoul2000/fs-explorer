(ns server.handler.run-command
  (:require [server.response :as response]
            [domain.command :as command]))

(defn create [options]
  (fn [request]
    (let [command-name (get-in request [:params :name])
          path         (get-in request [:params :path])]
      (response/ok {:result (command/run command-name path options)}))))