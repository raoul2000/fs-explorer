(ns server.handler.explorer
  (:require [server.response :as response]
            [domain.explorer.core :as exp]
            [config :as config]))

(defn create
  [{:keys [config]}]
  (fn [request]
    (let [dir-path      (get-in request [:params :dir])
          root-dir-path (config/root-dir-path config)]
      (tap> {:config        config
             :dir-path      dir-path
             :root-dir-path root-dir-path})
      (response/ok (exp/explore (or dir-path "/") {:root-dir-path root-dir-path})))))

(comment
  ((create {:a 1}) nil)
  (response/ok (exp/explore "" {:root-dir-path "c:\\tmp"}))

  (exp/explore "" {:root-dir-path "C:\\Users\\emmanuel.deveze"})
  ;;
  )
