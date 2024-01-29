(ns server.routes
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http.ring-middlewares :as ring-mw]
            [server.response :as response]
            [server.handler.greet :as greet-handler]
            [server.handler.info :as info-handler]
            [server.handler.download :as download-handler]
            [server.handler.home :as home-handler]))

(defn interceptor-chain [handler]
  [response/coerce-body
   response/content-neg-intc
   handler])

(defn create [options]
  (tap> options)
  (route/expand-routes
   #{["/"             :get  (home-handler/create options)                        :route-name    :home]
     ["/greet"        :get  (interceptor-chain (greet-handler/create options))   :route-name    :greet]
     ["/info"         :get  (interceptor-chain (info-handler/create options))    :route-name    :info]

     ["/download"     :get   [;; file-info interceptor will set the content-type of the response
                              ;; based on the extension of the file to download.
                              ;; If not set, content-type defaults to application/octet-stream
                              (ring-mw/file-info)
                              (download-handler/create options)]           :route-name :get-download]}))