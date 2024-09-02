(ns system
  "Integrant system definition"
  (:require [integrant.core :as ig]
            [io.pedestal.http :as http]
            [server.routes :as server-routes]
            [clojure.java.browse :refer [browse-url]]
            [babashka.fs :as fs]
            [config :as conf]))

;; system config ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def config
  ;; set when the application starts, before initialising the system
  {:app/cli-args    []

   ;; the default configuration - some parameters can be over written by user config
   :app/config      {:cli-args                 (ig/ref :app/cli-args)}

   :server/routes    {:config                  (ig/ref :app/config)
                      :some-route-param        12}

   :server/server    {:config                  (ig/ref :app/config)
                      ::http/routes            (ig/ref :server/routes)
                      ::http/secure-headers    {:content-security-policy-settings {:object-src "none"}}
                      ::http/resource-path     "/public"
                      ::http/type              :jetty
                      ::http/join?             true}})

;; key initializers function ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init-config [{:keys [cli-args]}]
  (conf/create-config (first cli-args)))

(defn init-server-routes [routes]
  (tap> {:server-route-config routes})
  (server-routes/create routes))

(defn init-server [{:keys [config] :as service-map}]
  (when (config/open-broser? config)
    (browse-url (config/browse-url config)))

  (-> service-map
      (dissoc :config)
      (assoc  ::http/port (config/server-port config))
      http/create-server
      http/start))

;; integrant Key initializer ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod ig/init-key :app/cli-args
  [_ cli-args]
  cli-args)

(defmethod ig/init-key :app/config
  [_ config]
  (init-config config))

(defmethod ig/init-key :server/routes
  [_ routes]
  (init-server-routes routes))

(defmethod ig/init-key  :server/server
  [_ service-map]
  (init-server service-map))

(defmethod ig/halt-key! :server/server [_ server]
  (http/stop server))

(comment
  ;; start the system
  (def system (ig/init config))

  ;; stop the system
  (ig/halt! system)
  ;;
  )

(def init ig/init)
