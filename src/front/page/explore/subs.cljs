(ns page.explore.subs
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
 ::explore
 (fn [db _]
   (:explore db)))

(defn <explore []
  @(re-frame/subscribe [::explore]))

(re-frame/reg-sub
 ::loading?
 (fn [db _]
   (:loading? db)))

(defn <loading? []
  @(re-frame/subscribe [::loading?]))

