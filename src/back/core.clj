(ns core
  (:require [system :refer [config init]]
            [user-config :as user-config]))


(defn -main [file-path & _args]
  (try
    (let [user-config (if file-path (user-config/load file-path)  {})]

      (-> config
          (assoc-in [:app/user-config  :user-config] user-config)
          (assoc-in [:app/config       :polite?]     true)
          #_init))

    (catch Exception e
      (println (format "Error : %s - cause : %s" (.getMessage e) (ex-data e))))))
