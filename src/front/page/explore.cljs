(ns page.explore
  (:require [re-frame.core :as re-frame]
            [ajax.core :as ajax]
            [ajax.edn :refer [edn-response-format edn-request-format]]
            [day8.re-frame.http-fx]))


(re-frame/reg-event-db
 ::ls-success
 (fn [db [_ success-response]]
   (tap> success-response)
   (-> db
       (assoc :explore (:model/content success-response))
       (assoc :loading? false))))

(re-frame/reg-event-db
 ::ls-failure
 (fn [db [_ error-response]]
   (-> db
       (assoc :explore nil)
       (assoc :loading? false))))

(re-frame/reg-event-fx
 ::ls
 (fn [{db :db} _]
   {:http-xhrio {:method          :get
                 :uri             "/explore"
                 :format          (edn-request-format)
                 :response-format (edn-response-format)
                 :on-success      [::ls-success]
                 :on-failure      [::ls-failure]}
    :db  (assoc db :loading? true)}))

(re-frame/reg-event-db
 ::explore
 (fn [db [_ event-arg]]
   (assoc db :explore ["a" "b"])))

(defn >explore []
  (re-frame/dispatch [::ls]))

(re-frame/reg-sub
 ::explore
 (fn [db _]
   (:explore db)))

(defn <explore []
  @(re-frame/subscribe [::explore]))

(re-frame/reg-sub
 :loading?
 (fn [db _]
   (:loading? db)))

(defn <loading? []
  @(re-frame/subscribe [:loading?]))


;; view ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
   (let [loading? (<loading?)]
     [:button
      {:disabled loading?
       :on-click #(>explore)}
      (if loading? "loading..." "explore")])
   [explorer-view]])