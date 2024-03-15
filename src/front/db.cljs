(ns db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]
            [ajax.edn :refer [edn-response-format edn-request-format]]
            [model :as model]))

;; spec db ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::explore     :model/content)
(s/def ::user-config :user-config/config)
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
                                      ::user-config]))

;; Default db ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def default-db {:current-route nil
                 :explore       []
                 :current-dir   "/"
                 :loading?      false
                 :search       {:visible?         false
                                :text-filter      ""
                                :dir-index        []}
                 :user-config  {}})
(comment
  (s/valid? ::db default-db)
  ;;
  )

;; spec interceptor ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
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
                       :format          (edn-request-format)
                       :response-format (edn-response-format)
                       :on-success      [::load-index-success]
                       :on-failure      [::load-index-failure]}]]}))

;; loading user-config  ...

(re-frame/reg-event-db
 ::load-user-config-success
 [check-spec-interceptor]
 (fn [db [_ success-response]]
   (-> db
       (assoc :user-config  (:response success-response)))))

(re-frame/reg-event-db
 ::load-user-config-failure
 [check-spec-interceptor]
 (fn [db [_ _error-response]]
   db))

(re-frame/reg-event-fx
 ::load-user-config
 (fn [_cofx _event]
   {:fx [[:http-xhrio {:method          :get
                       :uri             (str "/config")
                       :format          (edn-request-format)
                       :response-format (edn-response-format)
                       :on-success      [::load-user-config-success]
                       :on-failure      [::load-user-config-failure]}]]}))

;; initialize db  ...

(re-frame/reg-event-fx
 ::initialize
 (fn [_cofx _event]
   {:db  default-db
    :fx [[:dispatch [::load-index]]
         [:dispatch [::load-user-config]]]}))

(defn >initialize-db []
  (re-frame/dispatch [::initialize]))

;; subs ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 ::user-config
 (fn [db _]
   (:user-config db)))

(defn <user-config []
  @re-frame/subscribe [::user-config])


(re-frame/reg-sub
 ::initialized?
 (fn  [db _]
   (seq (get-in db [:search :dir-index]))))

(defn <db-initialized? []
  @(re-frame/subscribe [::initialized?]))

