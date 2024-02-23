(ns page.explore.route
  (:require [page.explore.view :as explore]))

(def route-id ::explore)

(defn is? [route-name]
  (= route-name route-id))

(def route ["/explore/*path"  {:name route-id
                               :view explore/page}])