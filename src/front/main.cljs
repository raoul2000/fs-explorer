(ns main
  (:require [reagent.dom :as rdom]
            [re-pressed.core :as rp]
            [re-frame.core :as re-frame]
            [route.core :refer [init-routes!]]
            [route.subs :refer [<current-route]]
            [db :refer [>initialize-db]]
            [components.navbar :refer [navbar]]))

(re-frame/reg-event-fx
 ::keypress-search
 (fn [_ _]
   (js/console.log "key event")))

(defn >initialize-key-event-handlers
  "see https://github.com/gadfly361/re-pressed
   
   Key codes can be found here : https://www.toptal.com/developers/keycode
   "
  []
  (js/console.log "initializing key event handler")
  (re-frame/dispatch-sync [::rp/add-keyboard-event-listener "keydown"
                           :clear-on-success-event-match     true])
  (re-frame/dispatch [::rp/set-keydown-rules {:event-keys
                                              [[[::keypress-search] [{:keyCode 70
                                                                      :ctrlKey true}]]]

                                              :prevent-default-keys
                                              [{:keyCode 70 
                                                :ctrlKey true}]}]))

;; :clear-keys [ [{:keyCode 65 ;; 'k'
                                                              ;;:ctrlKey true}]]

(defn main-view []
  (let [current-route (<current-route)]
    (tap> {:current-route current-route})
    (when-let [view-component (-> current-route :data :view)]
      [view-component (:parameters current-route)])))

(defn main-page []
  [:div
   [navbar]
   [:div.section {:style {:margin-top "40px"}}
    [main-view]]
   #_[:footer.footer
      [:div.content.has-text-centered "some text"]]])


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



