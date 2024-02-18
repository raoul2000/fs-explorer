(ns page.home
  (:require [re-frame.core :as re-frame]))


(defn home-page []
  [:div
   [:h1.title "This is home page"]
   [:button
    ;; Dispatch navigate event that triggers a (side)effect.
    {:on-click #(re-frame/dispatch [:routes/push-state :routes/sub-page2])}
    "Go to sub-page 2"]])