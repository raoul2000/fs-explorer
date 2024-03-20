(ns page.explore.event
  (:require [re-frame.core :as re-frame]
            [ajax.edn :refer [edn-response-format edn-request-format]]
            [day8.re-frame.http-fx]
            [db :refer [check-spec-interceptor]]
            [route.helper :refer [create-url-explore]]))


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
                 :uri             (str "/explore?dir=" path)
                 :format          (edn-request-format)
                 :response-format (edn-response-format)
                 :on-success      [::ls-success]
                 :on-failure      [::ls-failure]}
    :db  (-> db
             (assoc :loading?    true))}))

(defn >explore [path]
  (re-frame/dispatch [::ls path]))

(re-frame/reg-event-fx
 ::select-dir
 (fn [{db :db} [_ dir]]
   {:db (-> db
            (assoc :current-dir dir))
    :fx  [(when-not (= (:current-dir db) dir) [:http-xhrio {:method          :get
                                                            :uri             (str "/explore?dir=" dir)
                                                            :format          (edn-request-format)
                                                            :response-format (edn-response-format)
                                                            :on-success      [::ls-success]
                                                            :on-failure      [::ls-failure]}])]}))

(defn >select-dir [path]
  (re-frame/dispatch [::select-dir path]))

;; run a command -------------------------------------------------------------------------------------------------------

(re-frame/reg-event-db
 ::run-command-success
 (fn [db [_ success-response]]
   (tap> {:run-command-success success-response})
   db))


(re-frame/reg-event-db
 ::run-command-failure
 (fn [db [_ error-response]]
   (tap> {:run-command-failure error-response})
   db))


(re-frame/reg-event-fx
 ::run-command
 (fn [{db :db} [_ command-name path]]
   {:http-xhrio {:method          :get
                 :uri             (str "/cmd?path=" path "&name=" command-name)
                 :format          (edn-request-format)
                 :response-format (edn-response-format)
                 :on-success      [::run-command-success]
                 :on-failure      [::run-command-failure]}}))

(defn >run-command [command-name path]
  (re-frame/dispatch [::run-command command-name path]))