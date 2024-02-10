(ns timer
  (:require  [reagent.dom :as rdom]
             [reagent.dom.client :as rdc]
             (clojure.string :as s)
             [re-frame.core :as rf]
             [cljs.pprint :refer [pprint]]))


(enable-console-print!)

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:click-count 0}))

(rf/reg-event-db
 :clicked
 (fn [db [_ event-arg]]
   (update db :click-count inc)))

(rf/reg-event-fx
 :reset-count
 (fn [cofx [_event-id event-arg]]
   (pprint cofx)
   {:db (assoc (:db cofx) :click-count 0)}))

(rf/reg-sub
 :click-count
 (fn [db _]
   (:click-count db)))

;; ---------------------------------------

(defn click-counter []
  (let [click-count @(rf/subscribe [:click-count])]
    [:div
     [:h1  click-count]
     [:div
      [:button {:on-click #(rf/dispatch [:clicked])} "Click Me"]
      [:button {:on-click #(rf/dispatch [:reset-count])} "Reset"]]]))

(defn view []
  (click-counter))

(defn render [element-id]
  (rf/dispatch-sync [:initialize])
  (rdom/render [view] (js/document.getElementById element-id)))

