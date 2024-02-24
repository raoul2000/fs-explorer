(ns page.explore.route
  (:require [page.explore.view :as explore]
            [route.helper :refer [href]]))

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
                              :view        explore/page
                              :controllers route-controllers}])

(defn create-url [dir-path]
  (href route-id {:path dir-path} {:extra-query-param "any value"}))