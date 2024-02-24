(ns page.about.route)

(def route-id ::about)

(defn is? [route-name]
  (= route-name route-id))

(def route ["/about" {:name route-id}])
