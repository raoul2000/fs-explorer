(ns server.routes
  (:require [io.pedestal.http.route :as route]
            [server.response :as response]
            [server.handler.greet :as greet-handler]
            [server.handler.info :as info-handler]))

(defn interceptor-chain [handler]
  [response/coerce-body
   response/content-neg-intc
   handler])

(defn create [options]
  (tap> options)
  (route/expand-routes
   #{["/greet" :get  (interceptor-chain (greet-handler/create options))   :route-name    :greet]
     ["/info"  :get  (interceptor-chain (info-handler/create options))    :route-name    :info]}))