(ns server.handler.greet
  (:require [server.response :as response]
            [domain.say-hello :refer (say-hello)]))

(defn create [{:keys [polite?] :as options}]
  (fn [request]
    (let [name (get-in request [:params :name])]
      (try
        (response/ok {:response (say-hello name polite?)})
        (catch Exception ex
          (response/error-SERVER_ERROR {:error (ex-message ex)}))))))