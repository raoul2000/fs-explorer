(ns main
  (:require [reagent.dom :as rdom]
            [re-frame.core :as re-frame]
            [route.core :refer [init-routes!]]
            [route.subs :refer [<current-route]]
            [db :refer [>initialize-db]]
            [components.navbar :refer [navbar]]))

(defn main-page
  "Displays the main page depending on the *current-route* provided by subscription."
  []
  (js/console.log "rendering main-page")
  (let [current-route (<current-route)]
    [:div.section {:style {:margin-top "40px"}}
     [navbar current-route]
     (when-let [view-component (-> current-route :data :view)]
       [view-component (:parameters current-route)])
     #_(when current-route
         [#((-> current-route :data :view) (:parameters current-route))])]))

(def debug? ^boolean goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (println "dev mode")))

(defn run []
  (re-frame/clear-subscription-cache!)
  (>initialize-db)
  (dev-setup)
  (init-routes!)
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



