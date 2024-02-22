(ns main
  (:require [reagent.dom :as rdom]
            [re-frame.core :as re-frame]
            [routes :as routes]
            [utils :refer [href]]
            [reitit.core :as r]
            [db :refer [default-db]]))


(re-frame/reg-event-db ::initialize-db
                       (fn [db _]
                         (if db
                           db
                           default-db)))

(defn nav
  "The navigation bar component"
  [{:keys [router current-route]}]
  [:ul
   (for [route-name (r/route-names router)
         :let       [route (r/match-by-name router route-name)
                     text (-> route :data :link-text)]]
     [:li {:key route-name}
      (when (= route-name (-> current-route :data :name))
        "> ")
      ;; Create a normal links that user can click
      [:a {:href (href route-name)} text]])])


(defn main-page []
  (let [current-route @(re-frame/subscribe [:routes/current-route])]
    [:div
     [nav {:router routes/router :current-route current-route}]
     [:hr]
     (when current-route
       [(-> current-route :data :view)])]))

(def debug? ^boolean goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (println "dev mode")))

(defn run []
  (re-frame/clear-subscription-cache!)
  (re-frame/dispatch-sync [::initialize-db])
  (dev-setup)
  (routes/init-routes!)
  (rdom/render [main-page] (.getElementById js/document "app")))


;;  Lifecycle Hooks ======================================================
;; see https://shadow-cljs.github.io/docs/UsersGuide.html#_lifecycle_hooks


(defn ^:dev/before-load stop []
  (js/console.log "/before-load"))


(defn ^:dev/after-load start []
  (js/console.log "after-load")
  (run))

(defn ^:dev/before-load-async async-stop [done]
  (js/console.log "(async) stop ")
  (js/setTimeout
   (fn []
     (js/console.log "(async) stop complete")
     (done))))

(defn ^:dev/after-load-async async-start [done]
  (js/console.log "(async) start")
  (js/setTimeout
   (fn []
     (js/console.log "(async) start complete")
     (done))))



