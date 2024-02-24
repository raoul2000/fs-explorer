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
                 :uri             (str "/explore/" path)
                 :format          (edn-request-format)
                 :response-format (edn-response-format)
                 :on-success      [::ls-success]
                 :on-failure      [::ls-failure]}
    :db  (-> db
             (assoc :loading?    true)
             (assoc :current-dir path))}))

(re-frame/reg-event-db
 ::explore
 (fn [db [_ event-arg]]
   (assoc db :explore ["a" "b"])))

(defn >explore [path]
  (re-frame/dispatch [::ls path]))
