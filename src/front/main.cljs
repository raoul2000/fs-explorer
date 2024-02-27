(ns main
  (:require [reagent.dom :as rdom]
            [re-frame.core :as re-frame]
            [route.core :refer [init-routes!]]
            [route.subs :refer [<current-route]]
            [route.helper :refer [home-route? about-route? explore-route? create-url-about create-url-explore create-url-home]]
            [page.explore.subs :refer [<current-dir]]
            [db :refer [>initialize-db]]))

(defn link-explore [route-name]
  (fn []
    (let [current-dir (<current-dir)]
      [:li (when (explore-route? route-name) ">") [:a {:href (create-url-explore current-dir)} "Explore"]])))

(defn nav
  "The navigation bar component"
  [current-route]
  (let [route-name (-> current-route :data :name)]
    [:div.title "navbar "
     [:ul
      [:li (when (home-route? route-name)    ">") [:a {:href (create-url-home)}       "home"]]
      [:li (when (about-route? route-name)   ">") [:a {:href (create-url-about)}      "about"]]
      [link-explore route-name]]]))

(defn main-page []
  (let [current-route (<current-route)]
    [:div
     [nav current-route]
     [:hr]
     (when current-route
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



