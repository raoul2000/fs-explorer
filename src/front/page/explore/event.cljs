(ns page.explore.event
  (:require [re-frame.core :as re-frame]
            [ajax.edn :refer [edn-response-format edn-request-format]]
            [ajax.json :refer [json-request-format json-response-format]]
            [day8.re-frame.http-fx]
            [db :refer [check-spec-interceptor]]
            [route.helper :refer [create-url-explore]]))


(re-frame/reg-event-db
 ::ls-success
 [check-spec-interceptor]
 (fn [db [_ success-response]]
   (-> db
       (assoc :explore (:content success-response))
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
                 :format          (json-request-format)
                 :response-format (json-response-format {:keywords? true})
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
                                                            :format          (json-request-format)
                                                            :response-format (json-response-format {:keywords? true})
                                                            :on-success      [::ls-success]
                                                            :on-failure      [::ls-failure]}])]}))

(defn >select-dir [path]
  (re-frame/dispatch [::select-dir path]))

;; run an action  -------------------------------------------------------------------------------------------------------

(re-frame/reg-event-db
 ::run-action-success
 (fn [db [_ success-response]]
   (tap> success-response)
   (when-let [download-params (get-in success-response [:result :redirect])]
     (.open js/window download-params (get-in success-response [:result :target] "_blank")))
   db))

(re-frame/reg-event-db
 ::run-action-failure
 (fn [db [_ error-response]]
   db))

;; voir https://github.com/JulianBirch/cljs-ajax/issues/167#issuecomment-293274030

(re-frame/reg-event-fx
 ::run-action
 (fn [{db :db} [_ action-name path]]
   {:http-xhrio {:method          :get
                 :uri             (str "/action?path=" (js/encodeURIComponent path)
                                       "&name=" (js/encodeURIComponent action-name))
                 :format          (json-request-format)
                 :response-format (json-response-format {:keywords? true})
                 :on-success      [::run-action-success]
                 :on-failure      [::run-action-failure]}}))

(defn >run-action [action-name path]
  (re-frame/dispatch [::run-action action-name path]))