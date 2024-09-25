(ns db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]
            [ajax.edn :refer [edn-response-format edn-request-format]]
            [ajax.json :refer [json-request-format json-response-format]]
            [model :as model]
            [oxbow.re-frame :as o] ;;sse
            ))

;; spec db ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::explore     :model/content)
(s/def ::config :user-config/config)
(s/def ::current-dir string?)
(s/def ::loading?    boolean?)

;; search feature

(s/def ::visible?    boolean?)
;; the search value
(s/def ::text-filter string?)
;; complete list of dis. This list is filtered by user input in the search modal
(s/def ::dir-index   (s/coll-of string? :kind vector?))
(s/def ::search      (s/keys :req-un [::visible?
                                      ::dir-index
                                      ::text-filter]))

(s/def ::db          (s/keys :req-un [::loading?
                                      ::explore
                                      ::current-dir
                                      ::search
                                      ::config]))

;; Default db ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def default-db {:current-route nil
                 :explore       []
                 :current-dir   "/"
                 :loading?      false
                 :search        {:visible?         false
                                 :text-filter      ""
                                 :dir-index        []}
                 :config       {}
                 :server-event  {}})
(comment
  (s/valid? ::db default-db)
  ;;
  )

;; spec interceptor ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  true
  #_(when-not (s/valid? a-spec db)
      (throw (ex-info (str "spec check failed: " {:cause (s/explain-data a-spec db)}) {}))))

;; now we create an interceptor using `after`
(def check-spec-interceptor (re-frame/after (partial check-and-throw ::db)))


;; event ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; loading dir index  ...

(re-frame/reg-event-db
 ::load-index-success
 [check-spec-interceptor]
 (fn [db [_ success-response]]
   (-> db
       (assoc-in [:search :dir-index] (vec (:index success-response))))))

(re-frame/reg-event-db
 ::load-index-failure
 [check-spec-interceptor]
 (fn [db [_ _error-response]]
   (-> db
       (assoc-in [:search :dir-index] []))))

(re-frame/reg-event-fx
 ::load-index
 (fn [_cofx _event]
   {:fx [[:http-xhrio {:method          :get
                       :uri             (str "/index?type=dir")
                       :format          (json-request-format)
                       :response-format (json-response-format {:keywords? true})
                       :on-success      [::load-index-success]
                       :on-failure      [::load-index-failure]}]]}))

;; loading config  ...

(re-frame/reg-event-db
 ::load-config-success
 [check-spec-interceptor]
 (fn [db [_ success-response]]
   (assoc db :config  (get-in success-response [:response :config]))))


(re-frame/reg-event-db
 ::load-config-failure
 [check-spec-interceptor]
 (fn [db [_ _error-response]]
   db))

(re-frame/reg-event-fx
 ::load-config
 (fn [_cofx _event]
   {:fx [[:http-xhrio {:method          :get
                       :uri             (str "/config")
                       :format          (json-request-format)
                       :response-format (json-response-format {:keywords? true})
                       :on-success      [::load-config-success]
                       :on-failure      [::load-config-failure]}]]}))

;; initialize db  ...

(re-frame/reg-event-fx
 ::initialize
 (fn [_cofx _event]
   {:db  default-db
    :fx [[:dispatch [::load-index]]
         [:dispatch [::load-config]]]}))

(defn >initialize-db []
  (re-frame/dispatch [::initialize]))

;; subs ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; the complete configuration

(re-frame/reg-sub
 ::config
 (fn [db _]
   (:config db)))

(defn <config []
  @(re-frame/subscribe [::config]))


;; part of the configuration

(re-frame/reg-sub
 ::config-actions
 :<- [::config]
 (fn [user-config]
   (:actions user-config)))

(defn <config-actions []
  @(re-frame/subscribe [::config-actions]))


(re-frame/reg-sub
 ::initialized?
 (fn  [db _]
   (seq (get-in db [:search :dir-index]))))

(defn <db-initialized? []
  @(re-frame/subscribe [::initialized?]))

(re-frame/reg-event-db
 ::on-count-tick
 (fn [db [_ {:keys [data] :as event}]]
   (js/console.log event)
   (update db :counter-value conj data)))

(defn >start-counting []
  (re-frame/dispatch [::o/sse-client {:id       ::counter-events
                                      :uri      "/event"
                                      :on-event [::on-count-tick]}]))


(defn >stop-counting []
  (re-frame/dispatch [::o/abort ::counter-events]))