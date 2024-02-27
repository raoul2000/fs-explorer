(ns db
  (:require [cljs.reader]
            [re-frame.core :refer [after]]
            [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]
            [model :as model]))

;; spec db -------------------------------------------------------------------------------

(s/def ::explore     :model/content)
(s/def ::current-dir string?)
(s/def ::loading?    boolean?)
(s/def ::db          (s/keys :req-un [::loading? ::explore ::current-dir]))

;; Default db ----------------------------------------------------------------------------

(def default-db {:current-route nil
                 :explore       []
                 :current-dir   ""
                 :loading?      false})

;; spec interceptor -----------------------------------------------------------------------

(defn- check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " {:cause (s/explain-data a-spec db)}) {}))))

;; now we create an interceptor using `after`
(def check-spec-interceptor (after (partial check-and-throw ::db)))

;; event ---------------------------------------------------------------------------------

(re-frame/reg-event-db ::initialize
                       (fn [db _]
                         (tap> db)
                         default-db))

(defn >initialize-db []
  (re-frame/dispatch-sync [::initialize]))
