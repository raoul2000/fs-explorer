(ns page.explore.subs
  (:require [re-frame.core :as re-frame]))


(re-frame/reg-sub
 ::explore
 (fn [db _]
   (:explore db)))

(defn <explore []
  @(re-frame/subscribe [::explore]))

(re-frame/reg-sub
 ::sorted-explore
 :<- [::explore]
 (fn [explore]
   (sort-by :file/dir? (fn [dir-a? dir-b?]
                         (cond
                           (and dir-a? dir-b?) 0
                           dir-a?              -1
                           :else               1)) explore)))

(defn <sorted-explore []
  @(re-frame/subscribe [::sorted-explore]))

(re-frame/reg-sub
 ::loading?
 (fn [db _]
   (:loading? db)))

(defn <loading? []
  @(re-frame/subscribe [::loading?]))

(re-frame/reg-sub
 ::current-dir
 (fn [db _]
   (:current-dir db)))

(defn <current-dir []
  @(re-frame/subscribe [::current-dir]))

