(ns routes
  "Routes management based on https://github.com/metosin/reitit/tree/master/examples/frontend-re-frame"
  (:require [re-frame.core :as re-frame]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]
            [reitit.core :as rc]
            [page.home :refer (home-page)]
            [page.explore :refer (explore-page)]
            [reagent.core :as r]))

;;; Effects ;;;

;; Triggering navigation from events.

(re-frame/reg-fx :push-state
                 (fn [route]
                   (apply rfe/push-state route)))

;;; Events ;;;

(re-frame/reg-event-fx ::push-state
                       (fn [_ [_ & route]]
                         {:push-state route}))

(re-frame/reg-event-db ::navigated
                       (fn [db [_ new-match]]
                         (let [old-match   (:current-route db)
                               controllers (rfc/apply-controllers (:controllers old-match) new-match)]
                           (assoc db :current-route (assoc new-match :controllers controllers)))))

;;; Subscriptions ;;;

(re-frame/reg-sub ::current-route
                  (fn [db]
                    (:current-route db)))



;;; Views ;;;

(defn sub-page1 []
  [:div
   [:h1 "This is sub-page 1"]])

(defn sub-page2 []
  [:div
   [:h1 "This is sub-page 2"]])


;;; Routes ;;;

(defn about-handler []
  (print "about route handler"))

(def routes
  [;; by default, keyword ::home is expanded to {:name ::home}
   ["/"               ::home]
   ;; by default function is expanded into {:handler the-function}
   ["/about"          about-handler]
   ["/explore/*path"  ::explore]
   ["/item/:id"       {:name       ::item
                       :handler    #(print "the item route handler")
                       :parameters {:path {:id int?}}}]
   ["/greet"          {:name       ::greet}]])

(defn create-router [routes-definitions]
  (rf/router  routes-definitions))

(comment
  (def router (create-router routes))

  (rc/router-name router)
  (rc/routes router)

  (rc/route-names router)

  (for [route-name (rc/route-names router)
        :let [route (rc/match-by-name router route-name)]]
    #_(prn route-name)
    (rfe/href route-name {:id 2} {:foo "bar"})
    )
  
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


(def routes-1
  ["/"
   [""
    {:name      ::home
     :view       home-page
     :link-text "Home"
     :controllers
     [{;; Do whatever initialization needed for home page
       ;; I.e (re-frame/dispatch [::events/load-something-with-ajax])
       :start (fn [& params] (js/console.log "Entering home page"))
       ;; Teardown can be done here.
       :stop  (fn [& params] (js/console.log "Leaving home page"))}]}]
   ["explore"
    {:name ::explore
     :view explore-page
     :link-text "Explore"}]
   ["sub-page1"
    {:name      ::sub-page1
     :view      sub-page1
     :link-text "Sub page 1"
     :controllers
     [{:start (fn [& params] (js/console.log "Entering sub-page 1"))
       :stop  (fn [& params] (js/console.log "Leaving sub-page 1"))}]}]
   ["sub-page2"
    {:name      ::sub-page2
     :view      sub-page2
     :link-text "Sub-page 2"
     :controllers
     [{:start (fn [& params] (js/console.log "Entering sub-page 2"))
       :stop  (fn [& params] (js/console.log "Leaving sub-page 2"))}]}]])

(defn on-navigate [new-match]
  (when new-match
    (re-frame/dispatch [::navigated new-match])))

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
