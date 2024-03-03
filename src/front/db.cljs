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

;; search feature

(s/def ::visible?    boolean?)
;; the search value
(s/def ::text-filter string?)
;; complete list of dis. This list is filtered by user input in the search modal
(s/def ::dir-index   (s/coll-of string?))
(s/def ::search      (s/keys :req-un [::visible?
                                      ::dir-index
                                      ::text-filter]))

(s/def ::db          (s/keys :req-un [::loading?
                                      ::explore
                                      ::current-dir
                                      ::search]))

;; Default db ----------------------------------------------------------------------------


(def default-db {:current-route nil
                 :explore       []
                 :current-dir   nil
                 :loading?      false
                 :search       {:visible?         true
                                :text-filter      ""
                                :dir-index        (take 100 (repeat "a/b/c"))
                                #_["a/b/c" "a/bd/e/f" "a/b/x/y" "x/y/z"
                                                   "a/b/c" "a/bd/e/f" "a/b/x/y" "x/y/z"
                                                   "a/b/c" "a/bd/e/f" "a/b/x/y" "x/y/z"
                                                   "a/b/c" "a/bd/e/f" "a/b/x/y" "x/y/z"
                                                   "a/b/c" "a/bd/e/f" "a/b/x/y" "x/y/z"]}})

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
                         default-db))

(defn >initialize-db []
  (re-frame/dispatch-sync [::initialize]))
