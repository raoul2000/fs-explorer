(ns server.handler.explorer
  (:require [server.response :as response]
            [domain.explorer :as exp]))

(defn create
  [{:keys [root-dir-path]}]
  (fn [request]
    (let [dir-path (get-in request [:params :dir])]
      (response/ok (exp/explore (or dir-path "/") {:root-dir-path root-dir-path})))))

(comment
  ((create {:a 1}) nil)
  (response/ok (exp/explore "tmp" {:root-dir-path "c:"}))
  ;;
  )
