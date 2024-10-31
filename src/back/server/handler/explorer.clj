(ns server.handler.explorer
  (:require [server.response :as response]
            [domain.explorer.core :as exp]
            [config :as config]))

(defn create
  [{:keys [config]}]
  (fn [request]
    (let [dir-path            (get-in request [:params :dir])
          root-dir-path       (config/root-dir-path       config)
          type-definitions    (config/types-definition    config)
          metadata-definition (config/metadata-definition config)]
      (response/ok (exp/explore (or dir-path "/") {:root-dir-path   root-dir-path
                                                   :types           type-definitions
                                                   :metadata        metadata-definition})))))

