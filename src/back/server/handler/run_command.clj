(ns server.handler.run-command
  (:require [server.response :as response]
            [domain.command :as command]))

(defn create [options]
  (fn [request]
    (let [command-name (get-in request [:params :name])
          path         (get-in request [:params :path])]
      (command/run command-name path options)
      (response/ok {:response {:command-name command-name
                               :path         path}}))))