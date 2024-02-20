(ns db
  (:require [cljs.reader]
            [re-frame.core :refer [after]]
            [cljs.spec.alpha :as s]
            [model :as model]))

;; spec db -------------------------------------------------------------------------------

(s/def ::explore   :model/content)
(s/def ::loading?  boolean?)
(s/def ::db        (s/keys :req-un [::loading? ::explore]))

;; Default db ----------------------------------------------------------------------------

(def default-db {:current-route nil
                 :explore       []
                 :loading?      false})

;; spec interceptor -----------------------------------------------------------------------

(defn- check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;; now we create an interceptor using `after`
(def check-spec-interceptor (after (partial check-and-throw ::db)))
