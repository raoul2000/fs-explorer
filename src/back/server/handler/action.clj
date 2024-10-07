(ns server.handler.action
  (:require [server.response :as response]
            [domain.action :as action]))

(defn create [options]
  (fn [request]
    (let [command-name (get-in request [:params :name])
          path         (get-in request [:params :path])]
      (response/ok {:result (action/run command-name path options)}))))