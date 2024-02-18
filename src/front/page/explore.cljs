(ns page.explore
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-event-db
 ::explore
 (fn [db [_ event-arg]]
   (assoc db :explore ["a" "b"])))

(re-frame/reg-sub
 :explore
 (fn [db _]
   (:explore db)))

(defn <explore []
  @(re-frame/subscribe [:explore]))


(defn view-item [item]
  [:li {:key item} item])

(defn explorer-view []
  [:div "explorer view"]
  (if-let [list-items (<explore)]
    [:div
     [:ul
      (map view-item list-items)]]
    [:div "empty"]))


(defn explore-page []
  [:div
   [:h1.title "FS Explorer"]
   [:button
    {:on-click #(re-frame/dispatch [::explore])}
    "explore"]
   [explorer-view]])