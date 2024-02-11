(ns core
  "Main namespace. Include the application entry point function"
  (:require [system :as  sys]
            [user-config :as user-config])
  (:gen-class))

(defn -main [& args]
  (try
    (let [file-path   (first args)
          user-config (when file-path (user-config/load-from-file file-path))]

      (-> sys/config
          (assoc    :app-1/user-config   (or user-config {}))
          (assoc-in [:app-1/config       :polite?]     true)
          sys/init))

    (catch Exception e
      (println (format "Error : %s - cause : %s" (.getMessage e) (ex-data e))))))

(comment

  (-main "")
  (-main "test/back/fixture/config-ok.json")
  (-main "test/back/fixture/config-invalid-port.json")
  ;;
  )
