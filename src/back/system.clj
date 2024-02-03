(ns system
  "Integrant system definition"
  (:require [integrant.core :as ig]
            [io.pedestal.http :as http]
            [server.routes :as server-routes]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def config
  {:app/user-config {:user-config              {}}

   ;; the default configuration - some parameters can be over written by user-config
   :app/config      {:user-config              (ig/ref :app/user-config)
                     :param1                   "value1"
                     :param2                   {:nested-p1 true
                                                :nested-p2 12
                                                :nested-p3 "some string"}
                     :polite?                  false
                     :nice-goodbye?            false
                     :port                     8890}

   :server/routes    {:config                  (ig/ref :app/config)}

   :server/server    {:config                  (ig/ref :app/config)
                      ::http/routes            (ig/ref :server/routes)
                      ::http/secure-headers    {:content-security-policy-settings {:object-src "none"}}
                      ::http/resource-path     "/public"
                      ::http/type              :jetty
                      ::http/join?             true}})

(defmethod ig/init-key :app/user-config
  [_ user-config]
  user-config)

(defn init-app-config
  "Create the config map from the given map *m*. 
   In particular merges user-config with default config"
  [m]
  (let [user-config (:user-config m)]
    (-> m
        (dissoc :user-config)
        (update :port #(or (:user-config/server-port user-config) %)))))

(defmethod ig/init-key :app/config
  [_ config]
  (init-app-config config))

(defmethod ig/init-key :server/routes
  [_ {:keys [config]}]
  (server-routes/create config))

(defmethod ig/init-key  :server/server
  [_ service-map]
  (print "starting server ...")
  (-> service-map
      (assoc  ::http/port (get-in service-map [:config :port]))
      (dissoc :config)
      http/create-server
      http/start))

(defmethod ig/halt-key! :server/server [_ server]
  (print "halting server ...")
  (http/stop server))

(comment
  ;; start the system
  (def system (ig/init config))

  ;; stop the system
  (ig/halt! system)
  ;;
  )

(def init ig/init)
