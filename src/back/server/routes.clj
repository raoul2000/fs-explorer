(ns server.routes
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http.ring-middlewares :as ring-mw]
            [io.pedestal.interceptor.error :refer (error-dispatch)]
            [server.response :as response]
            ;; server routes
            [server.handler.greet :as greet-handler]
            [server.handler.info :as info-handler]
            [server.handler.download :as download-handler]
            [server.handler.home :as home-handler]
            [server.handler.config :as config-handler]
            [server.handler.explorer :as explorer-handler]
            [server.handler.index :as index-handler]
            [server.handler.event :as event-handler]
            [server.handler.open-file :as open-file]
            [server.handler.action :as action]
            ;; sse
            [io.pedestal.http.sse :as sse]))

(def service-error-handler
  "Error handler based on http://pedestal.io/pedestal/0.6/reference/error-handling.html"
  (error-dispatch [ctx ex]
                  [{:exception-type :clojure.lang.ExceptionInfo}]
                  (do
                    #_(tap> {:ex-data    (ex-data ex)
                             :ex-message (ex-message ex)
                             :ex-cause   (ex-cause ex)})
                    (if-let [ex-from-logic (ex-cause ex)]
                      ;; extract the first cause in the chain which is the ex-info thrown by the logic layer
                      (assoc ctx :response (response/error-BAD_REQUEST (response/error-body (ex-message ex-from-logic)
                                                                                            (ex-data ex-from-logic)))

                             #_{:status 400 :body (response/error-body (ex-message ex-from-logic)
                                                                       (ex-data ex-from-logic))})
                      (assoc ctx :response (response/error-SERVER_ERROR (ex-message ex))
                             #_{:status 400 :body (response/error-body (ex-message ex))})))

                  :else
                  (assoc ctx :io.pedestal.interceptor.chain/error ex)))


(defn interceptor-chain [handler]
  [response/coerce-body
   response/content-neg-intc
   service-error-handler
   handler])

(defn create [route-config]
  (route/expand-routes
   #{["/"              :get  (home-handler/create route-config)                            :route-name    :home]
     ["/greet"         :get  (interceptor-chain (greet-handler/create  route-config))      :route-name    :greet]
     ["/info"          :get  (interceptor-chain (info-handler/create   route-config))      :route-name    :info]
     ["/config"        :get  (interceptor-chain (config-handler/create route-config))      :route-name    :config]

     ;; note: route'/explore/' is NOT valid
     ["/explore"       :get  (interceptor-chain (explorer-handler/create route-config))    :route-name    :explorer]
     ["/index"         :get  (interceptor-chain (index-handler/create    route-config))    :route-name    :index]
     ["/open"          :get  (interceptor-chain (open-file/create        route-config))    :route-name    :open-file]
     ["/action"        :get  (interceptor-chain (action/create      route-config))    :route-name    :run-command]

     ;; SSE notifier
     ["/event"         :get [(sse/start-event-stream event-handler/event-stream)]          :route-name    :get-event-stream]

     ["/download"      :get   [;; file-info interceptor will set the content-type of the response
                              ;; based on the extension of the file to download.
                              ;; If not set, content-type defaults to application/octet-stream

                               (ring-mw/file-info)
                               service-error-handler
                               (download-handler/create route-config)]                     :route-name    :get-download]}))


