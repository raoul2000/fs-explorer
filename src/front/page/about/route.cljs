(ns page.about.route
  (:require [page.about.view :as about]
            [route.helper :refer [href]]))

(def route-id ::about)

(defn is? [route-name]
  (= route-name route-id))

(def route ["/about" {:name route-id 
                      :view about/page}])

(defn create-url []
  (href route-id))