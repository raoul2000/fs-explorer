(ns integrant-sample
  (:require [integrant.core :as ig]))


(def config {:app-1/user-conf {:user-name "bob Dylan"}
             :app-1/config   {:param1 "value1"
                            :user-name "anonymous"
                            :user-conf (ig/ref :app-1/user-conf)}

             :app-1/config-2 {:param2 "value2"
                            :conf (ig/ref :app-1/config)}

             :app-1/main   {:conf1 (ig/ref :app-1/config)
                          :conf2 (ig/ref :app-1/config-2)}})


(defmethod ig/init-key :app-1/user-conf
  [_ config]
  (prn "init :app/user-conf")
  (print config)
  config)

(defmethod ig/init-key :app-1/config
  [_ config]
  (prn "init :app/config")
  (-> config
      (merge  (:user-conf config))
      (dissoc :user-conf)))

(defmethod ig/init-key :app-1/config-2
  [_ config]
  (prn "init :app/config-2")
  (print config)
  config)

(defmethod ig/init-key :app-1/main
  [_ config]
  (prn "init :app/main")
  (print config)
  config)

(defmethod ig/halt-key! :app-1/main
  [_ config]
  (prn config)
  config)

(comment

  (def system (-> config
                  (assoc :app-1/user-conf {:user-name "Martin"
                                         :user-age 22})
                  ig/init))

  (ig/halt! system)
  (prn "eee")
  ;;
  )
