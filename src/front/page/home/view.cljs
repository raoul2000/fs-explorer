(ns page.home.view
  (:require [route.helper :as route]))

(defn page []
  (print "home route handler")
  [:div.title "home"
   [:hr]
   [:button {:on-click route/>navigate-to-about} "go to about"]])
