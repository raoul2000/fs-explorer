(ns page.about.view
  (:require [re-frame.core :as rfc]))

(defn page []
  (print "about route handler")
  [:div.title "About 1"
   [:button {:on-click #(rfc/dispatch [:route.event/push-state :page.home.route/home])}
    "go to homepage"]])