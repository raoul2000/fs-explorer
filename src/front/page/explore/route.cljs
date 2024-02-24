(ns page.explore.route)

(def route-id ::explore)

(defn is? [route-name]
  (= route-name route-id))

;; about route controllers see https://github.com/metosin/reitit/blob/master/doc/frontend/controllers.md
(def route-controllers  [{:parameters {:path [:path]}
                          :start      (fn [params]
                                        (js/console.log "Entering page explorer path = " (-> params :path :path)))
                        ;; Teardown can be done here.
                          :stop       (fn [& params] (js/console.log "Leaving home page"))}])

(def route ["/explore/*path" {:name        route-id
                              :controllers route-controllers}])



