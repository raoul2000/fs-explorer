(ns core
  "Main namespace. Include the application entry point function"
  (:require [system :as  sys])
  (:gen-class))

(defn -main [& args]
  (try
    (-> sys/config
        (assoc  :app/cli-args args)
        ;; late hard coded config overwrite TODO: remove
        (assoc-in [:app/config       :polite?]     true)
        sys/init)
    (catch Exception e
      (println (format "Error : %s - cause : %s" (.getMessage e) (ex-data e))))))

(comment

  (-main "")
  (-main "test/back/fixture/config-ok.json")
  (-main "test/back/fixture/config-invalid-port.json")

  (defn f [& args]
    args)

  (f 1 2 3)

  ;;
  )
