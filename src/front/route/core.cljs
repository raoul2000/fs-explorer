(ns route.core
  (:require [re-frame.core :as re-frame]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.spec :as rss]
            [page.about.view :as about]
            [page.explore.view :as explore]
            [page.home.view :as home]
            [route.event :as event-to-load]))


(def routes
  [(about/create-route)
   (explore/create-route)
   (home/create-route)])

(defn on-navigate [new-match]
  (when new-match
    (re-frame/dispatch [:route.event/navigated new-match])))

(def router
  (rf/router
   routes
   {:data {:coercion rss/coercion}}))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   router
   on-navigate
   {:use-fragment true}))




