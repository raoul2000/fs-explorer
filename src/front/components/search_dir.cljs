(ns components.search-dir
  (:require [reagent.dom :as rdom]
            [clojure.string :as str]
            [re-pressed.core :as rp]
            [re-frame.core :as re-frame]
            [route.core :refer [init-routes!]]
            [route.subs :refer [<current-route]]
            [db :refer [>initialize-db]]
            [components.navbar :refer [navbar]]))


;; Events ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-db
 ::update-search-filter
 (fn [db [_ new-value]]
   (assoc-in db [:search :text-filter] new-value)))

(defn >update-text-filter
  "The dir search filter value changed"
  [s]
  (re-frame/dispatch [::update-search-filter s]))


;; Subscriptions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-sub
 ::show-search
 (fn [db _]
   (get-in db [:search :visible?])))

(defn <show-search?
  "should the search modal be displayed ?"
  []
  @(re-frame/subscribe [::show-search]))

(re-frame/reg-sub
 ::dir-index
 (fn [db _]
   (get-in db [:search :dir-index])))

(re-frame/reg-sub
 ::text-filter
 (fn [db _]
   (get-in db [:search :text-filter])))


(re-frame/reg-sub
 ::selected-dirs

 :<- [::dir-index]
 :<- [::text-filter]

 (fn [[dir-index text-filter]]
   (filter #(str/starts-with? % text-filter) dir-index)))

(defn <selected-dirs
  "Returns the list of dir selected by the current search filter text"
  []
  @(re-frame/subscribe [::selected-dirs]))


;; components ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn search-results []
  (let [results (<selected-dirs)]
    [:div {:style {:max-height "100px"
                   :overflow "auto"}}
     [:ul
      (for [dir results]
        ^{:key dir} [:li dir])]]))

(defn modal-search
  "insert the dir search component. It is displayed depending on *db* state"
  []
  (when (<show-search?)
    [:div.modal.is-active {:style {:justify-content "flex-start"}}
     [:div.modal-background {:style {:background-color "rgba(0, 0, 0, 0.11)"}}]
     [:div.modal-content {:style {:width "50%"
                                  :top  "1em"}}
      [:div.box
       [:input.input.is-medium {:type        "text"
                                :auto-focus  true
                                :placeholder "enter search "
                                :on-change   (fn [e]
                                               (>update-text-filter (-> e .-target .-value)))}]
       [search-results]]]]))