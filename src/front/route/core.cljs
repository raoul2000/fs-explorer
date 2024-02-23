(ns route.core
  (:require [re-frame.core :as re-frame]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.spec :as rss]
            [route.helper :refer [href]]
            [page.about.view :as about]
            [page.about.route :as about-route]
            [page.home.route :as home-route]
            [page.explore.route :as explore-route]
            [route.event :as route-events]))

(def routes_v2
  [;; by default, keyword ::home is expanded to {:name ::home}
   #_["/"               {:name ::home
                         :view  home-handler}]
   home-route/route
   ;; by default function is expanded into {:handler the-function}
   about-route/route
   #_["/about"          {:name ::about
                         :view about/page}]
   #_["/explore/*path"  {:name ::explore
                         :view explore-handler}]
   #_["/item/:id"       {:name       ::item
                         :handler    #(print "the item route handler")
                         :parameters {:path {:id int?}}}]
   #_["/greet"          {:name       ::greet}]])

(def routes_v3
  [;; by default, keyword ::home is expanded to {:name ::home}

   ;; by default function is expanded into {:handler the-function}
   about-route/route
   home-route/route

   #_["/about"          {:name ::about
                         :view about/page}]

   #_["/item/:id"       {:name       ::item
                         :handler    #(print "the item route handler")
                         :parameters {:path {:id int?}}}]
   #_["/greet"          {:name       ::greet}]])

(def routes
  [about-route/route
   home-route/route
   explore-route/route])

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
  (def router (create-router routes))

  (rc/router-name router)
  (rc/routes router)

  (rc/route-names router)

  (for [route-name (rc/route-names router)
        :let [route (rc/match-by-name router route-name)]]
    #_(prn route-name)
    (rfe/href route-name {:id 2} {:foo "bar"}))

  (rc/match-by-name router ::home)
  (rc/match-by-path router "/")

  (rc/match-by-path router "/explore/dirname/file")
  (rc/match-by-name router ::explore {:path "dirname/filename"})

  (-> router
      (rc/match-by-name ::explore {:path "some-path/dirname"})
      (rc/match->path {:param1 "value1"}))

  (rc/match-by-path router "/greet")

  (defonce match (r/atom nil))
  @match


  (rfe/start!
   (rf/router routes {:data {:coercion rss/coercion}})
   (fn [m] (reset! match m))
      ;; set to false to enable HistoryAPI
   {:use-fragment true})


  (rfe/href ::item {:id 2})
  (rfe/href ::explore {:path "somepath/folder"} {:foo "bar"})
  ;; => "#/explore/somepath%2Ffolder?foo=bar"

  (rc/match-by-path router "/explore/somepath%2Ffolder?foo=bar")

  ;;
  )

