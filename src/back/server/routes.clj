(ns server.routes
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http.ring-middlewares :as ring-mw]
            [io.pedestal.interceptor.error :refer (error-dispatch)]
            [server.response :as response]
            [server.handler.greet :as greet-handler]
            [server.handler.info :as info-handler]
            [server.handler.download :as download-handler]
            [server.handler.home :as home-handler]
            [server.handler.config :as config-handler]
            [server.handler.explorer :as explorer-handler]
            
            ))

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
                      (assoc ctx :response {:status 400 :body {:error {:message (ex-message ex-from-logic)
                                                                       :info    (ex-data ex-from-logic)}}})
                      (assoc ctx :response {:status 400 :body {:error {:message (ex-message ex)}}})))

                  :else
                  (assoc ctx :io.pedestal.interceptor.chain/error ex)))


(defn interceptor-chain [handler]
  [response/coerce-body
   response/content-neg-intc
   service-error-handler
   handler])

(defn create [options]
  (tap> {:create-route-options options})
  (route/expand-routes
   #{["/"              :get  (home-handler/create options)                            :route-name    :home]
     ["/greet"         :get  (interceptor-chain (greet-handler/create options))       :route-name    :greet]
     ["/info"          :get  (interceptor-chain (info-handler/create options))        :route-name    :info]
     ["/config"        :get  (interceptor-chain (config-handler/create options))      :route-name    :config]

     ["/explore/*path" :get  (interceptor-chain (explorer-handler/create options))    :route-name    :explorer]

     ["/download"      :get   [;; file-info interceptor will set the content-type of the response
                              ;; based on the extension of the file to download.
                              ;; If not set, content-type defaults to application/octet-stream
                              (ring-mw/file-info)
                              (download-handler/create options)]                      :route-name    :get-download]}))


