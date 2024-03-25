(ns server.handler.index
  (:require [server.response :as response]
            [domain.explorer.core :as explorer]))


(defn create
  [{:keys [root-dir-path]}]
  (fn [request]
    (let [index-type (get-in request [:params :type])]
      (response/ok (explorer/index index-type {:root-dir-path root-dir-path})))))
