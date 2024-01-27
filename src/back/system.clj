(ns system
  (:require [integrant.core :as ig]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))



(defn create-greet-handler [{:keys [polite?] :as options}]
  (fn [request]
    {:status 200 :body (if polite? "Good morning " "Hi")}))

(defn create-routes [options]
  (tap> options)
  #{["/greet" :get
     (create-greet-handler options)
     :route-name :greet]})

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
                      ::http/resource-path     "/public"
                      ::http/type              :jetty
                      ::http/port              8890
                      ::http/join?             false}})

(defmethod ig/init-key :app/config
  [_ config]
  config)

(defmethod ig/init-key :server/routes
  [_ {:keys [config]}]
  (-> config
      create-routes
      route/expand-routes))

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