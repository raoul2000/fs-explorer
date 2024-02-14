(ns server.handler.explorer
  (:require [server.response :as response]
            [babashka.fs :as fs]
            [domain.explorer :as exp]))

(defn create 
  [{:keys [root-dir-path]}]
  (fn [path]
    (response/ok (exp/explore (or path "c:\\tmp")))))

(comment
  ;;
  )
