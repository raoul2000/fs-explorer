(ns page.home.route)

(def route-id ::home)

(defn is? [route-name]
  (= route-name route-id))

(def route ["/"  {:name route-id}])

