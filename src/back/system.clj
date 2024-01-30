(ns system
  (:require [integrant.core :as ig]
            [io.pedestal.http :as http]
            [server.routes :as server-routes])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def config
  ;; the default configuration - can be over written by user config
  {:app/config      {:param1                   "value1"
                     :param2                   {:nested-p1 true
                                                :nested-p2 12
                                                :nested-p3 "some string"}
                     :polite?                  false
                     :nice-goodbye?            false}

   :server/routes    {:config                  (ig/ref :app/config)}

   :server/server    {::http/routes            (ig/ref :server/routes)
                       ::http/secure-headers   {:content-security-policy-settings {:object-src "none"}}
                      ::http/resource-path     "/public"
                      ::http/type              :jetty
                      ::http/port              8890
                      ::http/join?             true}})

(defmethod ig/init-key :app/config
  [_ config]
  config)

(defmethod ig/init-key :server/routes
  [_ {:keys [config]}]
  (server-routes/create config))

(defmethod ig/init-key  :server/server
  [_ service-map]
  (print "starting server ...")
  (http/start (http/create-server service-map)))

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

(defn -main []
  (-> config
      (assoc-in [:app/config :polite?] true)
      ig/init))