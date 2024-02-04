(ns server.routes
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http.ring-middlewares :as ring-mw]
            [io.pedestal.interceptor.error :refer (error-dispatch)]
            [server.response :as response]
            [server.handler.greet :as greet-handler]
            [server.handler.info :as info-handler]
            [server.handler.download :as download-handler]
            [server.handler.home :as home-handler]
            [server.handler.config :as config-handler]))

;; TODO: finish implementation
(def service-error-handler
  (error-dispatch [ctx ex]
                  [{:exception-type :java.lang.ArithmeticException :interceptor ::another-bad-one}]
                  (assoc ctx :response {:status 400 :body "Another bad one"})

                  [{:exception-type :clojure.lang.ExceptionInfo}]
                  (do
                    (tap> (ex-data ex))
                    (assoc ctx :response {:status 400 :body {:message (ex-message ex)
                                                             :info    "info"}}))

                  :else
                  (assoc ctx :io.pedestal.interceptor.chain/error ex)))

(defn interceptor-chain [handler]
  [response/coerce-body
   response/content-neg-intc
   service-error-handler
   handler])

(defn create [options]
  (route/expand-routes
   #{["/"             :get  (home-handler/create options)                        :route-name    :home]
     ["/greet"        :get  (interceptor-chain (greet-handler/create options))   :route-name    :greet]
     ["/info"         :get  (interceptor-chain (info-handler/create options))    :route-name    :info]
     ["/config"       :get  (interceptor-chain (config-handler/create options))    :route-name    :config]

     ["/download"     :get   [;; file-info interceptor will set the content-type of the response
                              ;; based on the extension of the file to download.
                              ;; If not set, content-type defaults to application/octet-stream
                              (ring-mw/file-info)
                              (download-handler/create options)]           :route-name :get-download]}))