(ns main
  (:require [reagent.dom :as rdom]
            [re-pressed.core :as rp]
            [re-frame.core :as re-frame]
            [route.core :refer [init-routes!]]
            [route.subs :refer [<current-route]]
            [db :refer [>initialize-db <db-initialized? >start-counting >stop-counting]]
            [components.navbar :refer [navbar]]
            [components.search-dir :refer [modal-search]]))

;; key handler ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn >initialize-key-event-handlers
  "see https://github.com/gadfly361/re-pressed
   
   Key codes can be found here : https://www.toptal.com/developers/keycode
   "
  []
  (re-frame/dispatch-sync [::rp/add-keyboard-event-listener "keydown"
                           :clear-on-success-event-match     true])
  (re-frame/dispatch      [::rp/set-keydown-rules {:event-keys
                                                   [[[:components.search-dir/show]    [{:keyCode 70
                                                                                        :ctrlKey true}]]

                                                    [[:components.search-dir/hide]    [{:keyCode 27}]]
                                                    #_[[::keypress-enter]  [{:keyCode 13}]]
                                                    ;;
                                                    ]

                                                   :always-listen-keys
                                                   [{:keyCode 27}
                                                    #_{:keyCode 13}]

                                                   :prevent-default-keys
                                                   [{:keyCode 70
                                                     :ctrlKey true}]}]))

;; main view components ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn main-view []
  (let [current-route (<current-route)]
    (tap> {:current-route current-route})
    (when-let [view-component (-> current-route :data :view)]
      [view-component (:parameters current-route)])))

(defn counter []
  [:div
   [:button {:on-click >start-counting} "start counting"]
   [:button {:on-click >stop-counting} "stop counting"]])

(defn main-page []
  (if (<db-initialized?)
    [:div
     [navbar]

     [:div.section {:style {:margin-top "40px"}}
      #_[counter]
      [main-view]]
     [modal-search]]
    ;; loading app in progress ...
    [:section.hero.is-fullheight
     [:div.hero-body
      [:div
       [:div.title "Loading "]
       [:div.subtitle "just a few seconds ..."]]]]))

(def debug? ^boolean goog.DEBUG)



(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (println "dev mode")))

(defn run []
  (re-frame/clear-subscription-cache!)
  (>initialize-db)
  (>initialize-key-event-handlers)
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



