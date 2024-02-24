(ns page.explore.route
  (:require [page.explore.view :as explore]
            [route.helper :refer [href]]))

(def route-id ::explore)

(defn is? [route-name]
  (= route-name route-id))

(def route ["/explore/*path"  {:name route-id
                               :view explore/page}])

(defn create-url [dir-path]
  (href route-id {:path dir-path} {:extra-query-param "any value"}))