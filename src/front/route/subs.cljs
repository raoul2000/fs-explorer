(ns route.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub ::current-route
                  (fn [db]
                    (:current-route db)))

(defn <current-route []
   @(re-frame/subscribe [::current-route]))