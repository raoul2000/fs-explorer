(ns server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn respond-hello [request]
  {:status 200 :body "Hello, world!"})  

(def routes
  (route/expand-routes
   #{["/greet" :get respond-hello :route-name :greet]}))

(def service-map
  "The main service map"
  {:env                     :prod
   ::http/routes            routes
   ::http/type              :jetty
   ::http/resource-path     "/public"    ;; serve static resources from /resources/public
                                         ;; http://localhost:8890/index.html

   ;; This is required for a static served HTML page to load JS
   ;; TODO: study this settings to use the appropriate values   
   ::http/secure-headers   {:content-security-policy-settings {:object-src "none"}}

   ;; uncomment to disable logging
   ;; ::http/request-logger nil
   ::http/port              8890})

(defn start [port]
  (-> service-map
      (merge {::http/port port})
      http/create-server
      http/start))

;; interactive development ----------------------------------------------

;; the one and only server - used during DEV
(defonce server (atom nil))

(defn start-dev []
  (reset! server (-> service-map
                     ;; overload some key to fit dev environment
                     (merge {:env         :dev
                             ::http/join? false})
                     http/default-interceptors
                     http/create-server
                     http/start)))

(defn stop-dev []
  (http/stop @server))

(defn restart []
  (stop-dev)
  (reset! server nil)
  (start-dev))
