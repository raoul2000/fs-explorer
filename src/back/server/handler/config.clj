(ns server.handler.config
  (:require [server.response :as response]))

(defn create [options]
  (fn [_request]
    (response/ok {:response options})))