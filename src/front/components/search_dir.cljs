(ns components.search-dir
  (:require [reagent.dom :as rdom]
            [reagent.core :as rc]
            [clojure.string :as str]
            [re-pressed.core :as rp]
            [re-frame.core :as re-frame]
            [route.core :refer [init-routes!]]
            [route.subs :refer [<current-route]]
            [db :refer [>initialize-db]]
            [components.navbar :refer [navbar]]
            [route.helper :refer [create-url-explore >navigate-to-explore]]))


;; Events ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(re-frame/reg-event-db
 ::hide
 (fn [db _]
   (assoc-in db [:search :visible?] false)))

(defn >hide []
  (re-frame/dispatch [::hide]))

(re-frame/reg-event-db
 ::show
 (fn [db _]
   (assoc-in db [:search :visible?] true)))

(defn >show []
  (re-frame/dispatch [::show]))


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

(defn <text-filter []
  @(re-frame/subscribe [::text-filter]))

(re-frame/reg-sub
 ::selected-dirs

 :<- [::dir-index]
 :<- [::text-filter]

 (fn [[dir-index text-filter]]
   (case text-filter
     "" []
     "*" dir-index
     (filterv #(str/starts-with? % text-filter) dir-index))))

(defn <selected-dirs
  "Returns a vector of dirs selected by the current search filter text"
  []
  @(re-frame/subscribe [::selected-dirs]))


;; components ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn render-result-item-with-selection [item selected]
  [:a {:id item
       :on-click  (fn [event]
                    (.preventDefault event)
                    (.stopPropagation event)
                    (>hide)
                    (>navigate-to-explore item)
                    (>update-text-filter item))
       :href      (create-url-explore item)
       :class     (str "dropdown-item " (when selected "is-active"))
       :key       item}
   item])

(defn ensure-selected-item-visible
  "Assumes that the value of the selected item is also its DOM element id"
  [item-index items]
  (when-let [selected-dom-element (->> item-index
                                       (get items)
                                       (.getElementById js/document))]
    (.scrollIntoView selected-dom-element #js{"block" "nearest"})))

(defn modal-search
  "insert the dir search component in the DOM. 
   
   It is displayed depending on *db* state that can be changed with `>show` and `>hide`"
  []
  (let [selected-index       (rc/atom 0)]
    (fn []
      (when (<show-search?)
        (let [selected-items      (<selected-dirs)
              text-filter         (<text-filter)
              filtered-item-count (count selected-items)

              ;; handle keyboard events
              on-arrow-up         (fn [e]
                                    (.preventDefault e)
                                    (.stopPropagation e)
                                    (when-not (zero? @selected-index)
                                      (swap! selected-index dec)
                                      (ensure-selected-item-visible @selected-index selected-items)))
              on-arrow-down       (fn [e]
                                    (.preventDefault e)
                                    (.stopPropagation e)
                                    (when-not (= filtered-item-count (inc @selected-index))
                                      (swap! selected-index inc)
                                      (ensure-selected-item-visible @selected-index selected-items)))
              on-enter            (fn []
                                    (when-let [selected-item (get selected-items @selected-index)]
                                      (>hide)
                                      (>navigate-to-explore selected-item)
                                      (>update-text-filter (get selected-items @selected-index))))]

          [:div.modal.is-active   {:style {:justify-content "flex-start"}}
           [:div.modal-background {:style {:background-color "rgba(0, 0, 0, 0.11)"}}]
           [:div.modal-content    {:style {:width "50%"
                                           :top   "1em"}}
            [:div.box
             [:input.input.is-medium {:type         "text"
                                      :value        text-filter
                                      :auto-focus   true
                                      :placeholder  "enter search "
                                      :on-key-down  (fn [e]
                                                      (case (.-code e)
                                                        "ArrowDown"  (on-arrow-down e)
                                                        "ArrowUp"    (on-arrow-up e)
                                                        "Enter"      (on-enter)
                                                        nil))
                                      :on-change   (fn [e]
                                                     (reset! selected-index 0)
                                                     (>update-text-filter (-> e .-target .-value)))}]
             (when-not (zero? filtered-item-count)
               [:div {:style {:max-height "100px"
                              :margin-top "10px"
                              :overflow   "auto"}}
                [:ul
                 (doall
                  (map-indexed (fn [index item]
                                 (render-result-item-with-selection item (= index @selected-index)))
                               selected-items))]])]]])))))


