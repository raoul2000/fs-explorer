(ns server.handler.explorer
  (:require [server.response :as response]
            [domain.explorer :as exp]))

(defn create
  [{:keys [root-dir-path]}]
  (fn [request]
    (let [dir-path (get-in request [:path-params :path])]
      (response/ok (exp/explore dir-path)))))

(comment
  ((create {:a 1}) nil)
  (response/ok (exp/explore "c:\\tmp"))
  ;;
  )
