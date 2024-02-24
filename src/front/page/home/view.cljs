(ns page.home.view
  (:require [page.home.route :refer [route]]
            [route.helper :as route]))

(defn page []
  (print "home route handler")
  [:div.title "home"
   [:hr]
   [:button {:on-click route/>navigate-to-about} "go to about"]])

;; navigation and route ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-route []
  (let [[path options] route]
    (vector path (-> options
                     (assoc :view page)))))