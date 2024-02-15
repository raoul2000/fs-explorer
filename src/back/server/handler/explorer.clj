(ns server.handler.explorer
  (:require [server.response :as response]
            [domain.explorer :as exp]))

(defn create
  [{:keys [root-dir-path]}]
  (fn [_request]
    (response/ok (exp/explore "c:\\tmp")))) 

(comment
  ((create {:a 1}) nil)
  (response/ok (exp/explore "c:\\tmp"))
  ;;
  )
