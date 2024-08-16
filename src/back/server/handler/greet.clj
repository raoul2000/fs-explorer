(ns server.handler.greet
  (:require [server.response :as response]
            [domain.say-hello :refer (say-hello)]))

(defn create [{:keys [polite?] :as options}]
  (fn [request]
    (let [name (get-in request [:params :name])]
      ;; note that the JSON data returned does not include namespace in keys
      ;; In case EDN id returns, keys are namespaced
      (response/ok {:my-ns/response (say-hello name polite?)
                    :my-ns/score  12}))))