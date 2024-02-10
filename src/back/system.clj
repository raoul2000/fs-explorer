(ns system
  "Integrant system definition"
  (:require [integrant.core :as ig]
            [io.pedestal.http :as http]
            [server.routes :as server-routes]
            [clojure.java.browse :refer [browse-url]]))

;; system config ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def default-server-port 8890)
(def default-browse-url (format "http://localhost:%d/" default-server-port))

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
                     :open-browser?            true
                     :browse-url               default-browse-url
                     :port                     default-server-port}

   :server/routes    {:config                  (ig/ref :app/config)}

   :server/server    {:config                  (ig/ref :app/config)
                      ::http/routes            (ig/ref :server/routes)
                      ::http/secure-headers    {:content-security-policy-settings {:object-src "none"}}
                      ::http/resource-path     "/public"
                      ::http/type              :jetty
                      ::http/join?             true}})


;; key initializers function ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init-app-config
  "Create the config map from the given map *m*. 
   In particular merges user-config with default config"
  [{:keys [user-config] :or {user-config {}} :as config-map}]
  (tap> config-map)
  (-> config-map
      (dissoc :user-config)
      (update :port           #(get user-config :user-config/server-port   %))
      (update :open-browser?  #(get user-config :user-config/open-browser  %))
      (update :browse-url     #(get user-config :user-config/browse-url    %))))

(defn init-server [{:keys [config] :as service-map}]
  (when (:open-browser? config)
    (browse-url (:browse-url config)))

  (-> service-map
      (dissoc :config)
      (assoc  ::http/port (:port config))
      http/create-server
      http/start))

(defn init-server-routes [config]
  (server-routes/create config))

;; integrant Key initializer ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod ig/init-key :app/user-config
  [_ user-config]
  user-config)

(defmethod ig/init-key :app/config
  [_ config]
  (init-app-config config))

(defmethod ig/init-key :server/routes
  [_ {:keys [config]}]
  (init-server-routes config))

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
