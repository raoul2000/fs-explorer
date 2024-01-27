(ns server.handler.greet
  (:require [server.response :as response]))

(defn create [{:keys [polite?] :as options}]
  (fn [request]
    (response/ok (if polite? "Good morning " "Hi"))))