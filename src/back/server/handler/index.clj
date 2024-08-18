(ns server.handler.index
  (:require [server.response :as response]
            [domain.explorer.core :as explorer]
            [config :as config]))


(defn create
  [{:keys [config]}]
  (fn [request]
    (let [index-type (get-in request [:params :type])]
      (response/ok (explorer/index index-type {:root-dir-path (config/root-dir-path config)})))))
