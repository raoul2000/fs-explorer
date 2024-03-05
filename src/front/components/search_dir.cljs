(ns components.search-dir
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
   (assoc-in db [:search :text-filter] new-value)))

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

(defn apply-filter [text-filter coll]
  (case text-filter
    "" []
    "*" coll
    (filterv #(str/starts-with? % text-filter) coll)))

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
(defn modal-search-test []
  (let [text-filter    (rc/atom "")
        selected-index (rc/atom 0)
        arrow-pressed? (rc/atom false)]
    (fn []
      (when (<show-search?)
        (let [all-items              (<dir-index)
              filtered-items         (apply-filter @text-filter all-items)
              filtered-item-count    (count filtered-items)]

          [:div.modal.is-active {:style {:justify-content "flex-start"}}
           [:div.modal-background {:style {:background-color "rgba(0, 0, 0, 0.11)"}}]
           [:div.modal-content    {:style {:width "50%"
                                           :top   "1em"}}
            [:div.box
             [:input.input {:type         "text"
                            :value        @text-filter
                            :auto-focus   true
                            :placeholder  "enter search ..."
                            :on-key-down  (fn [event]
                                            (case (.-code event)
                                              "ArrowDown"  (when-not (= filtered-item-count (inc @selected-index))
                                                             (cancel-event event)
                                                             (reset! arrow-pressed? true)
                                                             (swap! selected-index inc)
                                                             (reset! text-filter (get filtered-items @selected-index))
                                                             (ensure-selected-item-visible @selected-index filtered-items))

                                              "ArrowUp"    (js/console.log "up")
                                              "Enter"      (js/console.log "enter")
                                              nil))
                            :on-change    (fn [event]
                                            (if @arrow-pressed?
                                              (reset! arrow-pressed? false)
                                              (reset! text-filter (-> event .-target .-value))))}]

             ;; hint text

             [:div.is-size-7.has-text-right {:style {:margin-top "10px"}}
              "select : " [:span.tag.is-light "Enter"]
              " close : " [:span.tag.is-light "Esc"]]

             (when-not (zero? filtered-item-count)
               [:div {:style {:max-height "30vh"
                              :margin-top "10px"
                              :overflow   "auto"}}
                [:ul
                 (doall
                  (map-indexed (fn [index item]
                                 (render-item item (= index @selected-index)))
                               filtered-items))]])]]])))))



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
                                    (when-not (zero? @selected-index)
                                      (cancel-event e)
                                      (swap! selected-index dec)
                                      (ensure-selected-item-visible @selected-index selected-items)))
              on-arrow-down       (fn [e]
                                    (when-not (= filtered-item-count (inc @selected-index))
                                      (cancel-event e)
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

             ;; the input text control

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
             ;; hint text

             [:div.is-size-7.has-text-right {:style {:margin-top "10px"}}
              "select : " [:span.tag.is-light "Enter"]
              " close : " [:span.tag.is-light "Esc"]]

             ;; selected results from filter text

             (when-not (zero? filtered-item-count)
               [:div {:style {:max-height "30vh"
                              :margin-top "10px"
                              :overflow   "auto"}}
                [:ul
                 (doall
                  (map-indexed (fn [index item]
                                 (render-item item (= index @selected-index)))
                               selected-items))]])]]])))))

