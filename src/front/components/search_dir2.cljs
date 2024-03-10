(ns components.search-dir2
  (:require [reagent.core :as rc]
            [clojure.string :as str]
            [re-frame.core :as re-frame]
            [route.helper :refer [>navigate-to-explore]]))


;; Events ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
   (assoc-in db [:search :quick-filter] new-value)))

(defn >update-text-filter
  "The dir search filter value changed"
  [s]
  (re-frame/dispatch [::update-search-filter s]))

;; Subscriptions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(defn <dir-index []
  @(re-frame/subscribe [::dir-index]))

;; component ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ensure-selected-item-visible
  "Assumes that the value of the selected item is also its DOM element id"
  [item-index items]
  (when-let [selected-dom-element (->> item-index
                                       (get items)
                                       (.getElementById js/document))]
    (.scrollIntoView selected-dom-element #js{"block" "nearest"})))

(defn cancel-event
  "Apply `preventDefault` and `stopPropagation` to the given *js Event*"
  [e]
  (.preventDefault e)
  (.stopPropagation e))

(defn apply-filter [quick-filter coll]
  (case quick-filter
    "" []
    "*" coll
    (filterv #(str/starts-with? % quick-filter) coll)))

(defn render-item [item selected?]
  [:a {:id item
       :on-click  (fn [event]
                    (cancel-event event)
                    (>hide)
                    (>navigate-to-explore item)
                    (>update-text-filter item))
       :href      ""
       :class     (str "dropdown-item " (when selected? "is-active"))
       :key       item}
   item])

;; This is an implementation attempt to get the same behaviour as the youtube search 
;; Selecting via arrows should update the input text but not perform filtering
(defn modal-search []
  (let [quick-filter    (rc/atom {:text           ""
                                  :apply?         false
                                  :selected-index -1
                                  :filtered-items []})
        all-items      (<dir-index)]
    (fn []
      (when (<show-search?)
        (let [filtered-items      (:filtered-items @quick-filter)
              filtered-item-count (count filtered-items)

              ;; handle keyboard events

              on-arrow-up         (fn [event]
                                    (when-not (zero? (:selected-index @quick-filter))
                                      (cancel-event event)
                                      (swap! quick-filter (fn [old]
                                                            (let [new-selected-index (dec (:selected-index old))]
                                                              (assoc old
                                                                     :text           (get filtered-items new-selected-index)
                                                                     :apply?         false
                                                                     :selected-index new-selected-index))))
                                      (ensure-selected-item-visible (:selected-index @quick-filter) filtered-items)))

              on-arrow-down       (fn [event]
                                    (when-not (= filtered-item-count (inc (:selected-index @quick-filter)))
                                      (cancel-event event)
                                      (swap! quick-filter (fn [old]
                                                            (let [new-selected-index (inc (:selected-index old))]
                                                              (assoc old
                                                                     :text           (get filtered-items new-selected-index)
                                                                     :apply?         false
                                                                     :selected-index new-selected-index))))
                                      (ensure-selected-item-visible (:selected-index @quick-filter) filtered-items)))

              on-enter            (fn []
                                    (when-let [selected-item (get filtered-items (:selected-index @quick-filter))]
                                      (>hide)
                                      (>navigate-to-explore selected-item)))]

          [:div.modal.is-active {:style {:justify-content "flex-start"}}
           [:div.modal-background {:style {:background-color "rgba(0, 0, 0, 0.11)"}}]
           [:div.modal-content    {:style {:width "50%"
                                           :top   "1em"}}
            [:div.box
             [:input.input {:type         "text"
                            :value        (:text @quick-filter)
                            :auto-focus   true
                            :placeholder  "enter search ..."
                            :on-key-down  (fn [event]
                                            (case (.-code event)
                                              "ArrowDown"  (on-arrow-down event)
                                              "ArrowUp"    (on-arrow-up event)
                                              "Enter"      (on-enter)
                                              nil))
                            :on-change    (fn [event]
                                            (let [text (-> event .-target .-value)]
                                              (reset! quick-filter {:text           text
                                                                    :apply?         true
                                                                    :selected-index -1
                                                                    :filtered-items (apply-filter text all-items)})))}]
             ;; hint text

             [:div.is-size-7.has-text-right {:style {:margin-top "10px"}}

              "select : " [:span.tag.is-light "Enter"]
              " close : " [:span.tag.is-light "Esc"]]

             ;; filtered options values

             (when-not (zero? filtered-item-count)
               [:div {:style {:max-height "30vh"
                              :margin-top "10px"
                              :overflow   "auto"}}
                [:ul
                 (doall
                  (map-indexed (fn [index item]
                                 (render-item item (= index (:selected-index @quick-filter))))
                               filtered-items))]])]]])))))

