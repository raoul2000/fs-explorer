(ns page.explore.subs
  (:require [re-frame.core :as re-frame]
            [clojure.string :as s]))


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

(defn- split-db-path [db-path]
  (loop [crumbs (reverse (s/split db-path #"/"))
         result []]
    (if (empty? crumbs)
      (reverse result)
      (recur (rest crumbs)
             (conj result {:name (first crumbs)
                           :path (s/join "/" (reverse crumbs))})))))

(comment
  (split-db-path "/a/b/c")
  (split-db-path "a/b/c")
  ;;
  )

(re-frame/reg-sub
 ::breadcrumbs
 :<- [::current-dir]
 (fn [current-dir] ;; ex /A/B/C
   (split-db-path current-dir)))

(defn <breadcrumbs []
  @(re-frame/subscribe [::breadcrumbs]))

