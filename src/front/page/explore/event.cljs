(ns page.explore.event
  (:require [re-frame.core :as re-frame]
            [ajax.edn :refer [edn-response-format edn-request-format]]
            [day8.re-frame.http-fx]
            [db :refer [check-spec-interceptor]]))


(re-frame/reg-event-db
 ::ls-success
 [check-spec-interceptor]
 (fn [db [_ success-response]]
   (-> db
       (assoc :explore (:model/content success-response))
       (assoc :loading? false))))

(re-frame/reg-event-db
 ::ls-failure
 [check-spec-interceptor]
 (fn [db [_ error-response]]
   (-> db
       (assoc :explore [])
       (assoc :loading? false))))

(re-frame/reg-event-fx
 ::ls
 (fn [{db :db} [_event-id path]]
   {:http-xhrio {:method          :get
                 :uri             (str "/explore/" (:current-dir db))
                 :format          (edn-request-format)
                 :response-format (edn-response-format)
                 :on-success      [::ls-success]
                 :on-failure      [::ls-failure]}
    :db  (-> db
             (assoc :loading?    true))}))

(defn >explore [path]
  (re-frame/dispatch [::ls path]))

(re-frame/reg-event-db
 ::select-dir
 (fn [db [_ dir]]
   (assoc db :current-dir dir)))

(defn >select-dir [path]
  (re-frame/dispatch [::select-dir path]))

