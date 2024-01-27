(ns server.routes
  (:require [io.pedestal.http.route :as route]
            [server.handler.greet :as greet-handler]))

(defn create [options]
  (tap> options)
  (route/expand-routes
   #{["/greet" :get  (greet-handler/create options)   :route-name    :greet]}))