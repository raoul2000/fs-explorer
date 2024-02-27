(ns route.core
  (:require [re-frame.core :as re-frame]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.spec :as rss]
            [page.about.view :as about]
            [page.explore.view :as explore]
            [page.home.view :as home]
            [route.event :as route-event] ;; required
            [page.explore.event :refer [>explore >select-dir]]))


(def routes [["/about"
              {:name        :route/about
               :view        about/page}]

             ["/explore"
              {:name        :route/explore
               :view        explore/page
               :controllers [{:parameters {:query [:dir]}
                              :start      (fn [params]
                                            (let [dir-path-to-explore (-> params :query :dir)]
                                              (js/console.log "[explorer] -->>  path = " dir-path-to-explore)
                                              (>select-dir (or dir-path-to-explore "/"))))
                              ;; Teardown can be done here.
                              :stop       (fn [params]
                                            (js/console.log "[explorer] <<-- path = " (-> params :query :dir)))}]}]

             ["/"
              {:name       :route/home
               :view       home/page}]])

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

(comment
  (init-routes!)
  (rfe/href :route/explore {} {:dir "some/dir"})

  ;;
  )





