(ns page.about.view
  (:require [route.helper :as route]))

(defn page []
  (print "about route handler")
  [:div.title "About 1"
   [:button {:on-click #(route/>navigate-to-home)}
    "go to homepage"]
   [:br]
   [:button {:on-click #(route/>navigate-to-explore "/")}
    "go to explore"]])
