(ns main
  (:require [reagent.dom :as rdom]
            [re-frame.core :as re-frame] 
            [route.core :refer [init-routes!]] 
            [route.subs :refer [<current-route]]
            [page.about.route   :as about-route]
            [page.home.route    :as home-route]
            [page.explore.route :as explore-route]
            [db :refer [>initialize-db]]))

(defn nav
  "The navigation bar component"
  [current-route]
  (let [route-name (-> current-route :data :name)]
    [:div.title "navbar "
     [:ul
      [:li (when (home-route/is? route-name)    ">") [:a {:href (home-route/create-url)}  "home"]]
      [:li (when (about-route/is? route-name)   ">") [:a {:href (about-route/create-url)} "about"]]
      [:li (when (explore-route/is? route-name) ">") [:a {:href (explore-route/create-url "dir1/dir3")} "Explore"]]]]))

(defn main-page []
  (let [current-route (<current-route)]
    [:div
     [nav current-route]
     [:hr]
     (when current-route
       (tap> current-route)
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



