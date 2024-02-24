(ns page.about.view
  (:require [page.about.route :refer [route]]
            [route.helper :as route]))

(defn page []
  (print "about route handler")
  [:div.title "About 1"
   [:button {:on-click #(route/>navigate-to-about)}
    "go to homepage"]
   [:br]
   [:button {:on-click #(route/>navigate-to-explore "some/folder/here")}
    "go to explore"]])


;; navigation and route ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-route []
  (let [[path options] route]
    (vector path (-> options
                     (assoc :view page)))))

