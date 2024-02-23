(ns page.home.route
  (:require [page.home.view :as home]))

(def route-id ::home)

(defn is? [route-name]
  (= route-name route-id))

(def route ["/"  {:name route-id
                  :view home/page}])