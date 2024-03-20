(ns components.search-dir
  (:require [reagent.core :as rc]
            [clojure.string :as str]
            [re-frame.core :as re-frame]
            [route.helper :refer [>navigate-to-explore]]
            [utils :refer [cancel-event]]))

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



(defn apply-filter [quick-filter coll]
  (case quick-filter
    ""  []
    "*" coll
    (when (> (count quick-filter) 2)
      (filterv #(str/starts-with? % quick-filter) coll))))

(defn render-item [item index selected? on-click-select]
  [:a {:id item
       :on-click  (fn [event]
                    (cancel-event event)
                    (on-click-select index item))
       :href      ""
       :class     (str "dropdown-item " (when selected? "is-active"))
       :key       item} item])


(defn modal-search
  "Displays the *quick filter* modal box, allowing the user to search for a dir.
   The behavior of this component is trying to mimic youtube search input."
  []
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
                                                              (assoc  old
                                                                      :text           (get filtered-items new-selected-index)
                                                                      :apply?         false
                                                                      :selected-index new-selected-index))))
                                      (ensure-selected-item-visible (:selected-index @quick-filter) filtered-items)))

              on-arrow-down       (fn [event]
                                    (when-not (= filtered-item-count (inc (:selected-index @quick-filter)))
                                      (cancel-event event)
                                      (swap! quick-filter (fn [old]
                                                            (let [new-selected-index (inc (:selected-index old))]
                                                              (assoc  old
                                                                      :text           (get filtered-items new-selected-index)
                                                                      :apply?         false
                                                                      :selected-index new-selected-index))))
                                      (ensure-selected-item-visible (:selected-index @quick-filter) filtered-items)))

                                  ;; user click Enter to navigate to the selected item
              on-enter            (fn []
                                    (when-let [selected-item (get filtered-items (:selected-index @quick-filter))]
                                      (>hide)
                                      (>navigate-to-explore selected-item)))

                                  ;; user enters a new value in the filter text input
              on-change           (fn [event]
                                    (let [text (-> event .-target .-value)]
                                      (reset! quick-filter {:text           text
                                                            :apply?         true
                                                            :selected-index -1
                                                            :filtered-items (apply-filter text all-items)})))

                                  ;; user clicks on a item in the options list"
              on-click-select     (fn
                                    [index item]
                                    (>hide)
                                    (>navigate-to-explore item)
                                    (swap! quick-filter (fn [old]
                                                          (assoc  old
                                                                  :text           item
                                                                  :apply?         true
                                                                  :selected-index index))))]

          [:div.modal.is-active {:style {:justify-content "flex-start"}}
           [:div.modal-background {:style {:background-color "rgba(0, 0, 0, 0.11)"}}]
           [:div.modal-content    {:style {:width "50%"
                                           :top   "1em"}}
            [:div.box

             ;; input text

             [:input.input {:type         "text"
                            :value        (:text @quick-filter)
                            :auto-focus   true
                            :placeholder  "enter search ..."
                            :on-key-down  (fn [event]
                                            (case (.-code event)
                                              "ArrowDown"  (on-arrow-down event)
                                              "ArrowUp"    (on-arrow-up   event)
                                              "Enter"      (on-enter)
                                              nil))
                            :on-change    on-change}]

             ;; hint text

             [:div.is-size-7.is-flex.is-justify-content-space-between.has-text-grey-light {:style {:margin-top "10px"}}
              [:div (str filtered-item-count " item(s) selected")]
              [:div "select : " [:span.tag.is-light "Enter"]
               " close : " [:span.tag.is-light "Esc"]]]

             ;; filtered options values

             (when-not (zero? filtered-item-count)
               [:div {:style {:max-height "30vh"
                              :margin-top "10px"
                              :overflow   "auto"}}
                [:ul
                 (doall
                  (map-indexed (fn [index item]
                                 (render-item item index (= index (:selected-index @quick-filter)) on-click-select))
                               filtered-items))]])]]])))))

