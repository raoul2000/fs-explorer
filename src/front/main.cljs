(ns main
  (:require [reagent.dom :as rdom]
            [re-pressed.core :as rp]
            [re-frame.core :as re-frame]
            [route.core :refer [init-routes!]]
            [route.subs :refer [<current-route]]
            [db :refer [>initialize-db]]
            [components.navbar :refer [navbar]]
            [components.search-dir :refer [modal-search]]))

;; key handler ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-fx
 ::keypress-esc
 (fn [{db :db} _]
   {:db (assoc-in db [:search :visible?] false)}))

(re-frame/reg-event-fx
 ::keypress-search
 (fn [{db :db} _]
   {:db (assoc-in db [:search :visible?] true)}))


(defn >initialize-key-event-handlers
  "see https://github.com/gadfly361/re-pressed
   
   Key codes can be found here : https://www.toptal.com/developers/keycode
   "
  []
  (re-frame/dispatch-sync [::rp/add-keyboard-event-listener "keydown"
                           :clear-on-success-event-match     true])
  (re-frame/dispatch      [::rp/set-keydown-rules {:event-keys
                                                   [[[::keypress-search] [{:keyCode 70 :ctrlKey true}]]
                                                    [[::keypress-esc]    [{:keyCode 27}]]
                                                    #_[[::keypress-enter]  [{:keyCode 13}]]]

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

(defn main-page []
  [:div
   [navbar]
   [:div.section {:style {:margin-top "40px"}}
    [main-view]]
   [modal-search]
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



