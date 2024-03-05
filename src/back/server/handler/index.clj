(ns server.handler.index
  (:require [server.response :as response]
            [domain.explorer :as exp]))


(defn create
  [{:keys [root-dir-path]}]
  (fn [request]
    (let [index-type (get-in request [:params :type])] 
      (response/ok (exp/index index-type {:root-dir-path root-dir-path})))))
