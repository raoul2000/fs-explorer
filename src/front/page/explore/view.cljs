(ns page.explore.view
  (:require [components.icon :refer [file-icon folder-icon home-icon]]
            [components.message :refer [warning-message]]
            [page.config.subs :refer [<config]]
            [page.explore.event :refer [>run-command]]
            [page.explore.subs :refer [<breadcrumbs <loading? <sorted-explore]]
            [reagent.core :as r]
            [route.helper :refer [>navigate-to-explore create-url-explore]]
            [utils :refer [cancel-event]]))

#_(defn eval-selector-rule [[rule-name rule-arg] s]
    (cond
      (= rule-name :action.selector/equals)  (= rule-arg s)
      (= rule-name :action.selector/match)   (re-find (re-pattern rule-arg) s)
      :else (throw (ex-info "unkown selector rule type" {:selector-rule-name rule-name
                                                         :selector-rule-arg  rule-arg}))))

#_(defn selector-match [{id :file/id} selector-val]
    #_(tap> {:selector-match true
             :id id
             :sel selector-val
             :map? (map? selector-val)
             :filter (when (map? selector-val)
                       (filter #(eval-selector-rule % id) selector-val))})
    (cond
      (string? selector-val)   (= selector-val id)
      (map?    selector-val)   (filter #(eval-selector-rule % id) selector-val)
      :else                    (throw (ex-info "failed to apply selector" {:selector selector-val}))))




#_(defn file-action [item config-actions]
    (if-let   [{:keys [command]} (find-matching-command config-actions item)
               #_(first (filter (fn [{:keys [selector]}]
                                  (= (:file/name item) selector)) config-actions))]
    ;; create anchor element's attributes for the selected action
      {:href  ""
       :on-click (fn [event]
                   (cancel-event event)
                   (js/console.log (str "running command " command))
                   (>run-command command (:id item)))}

    ;; default action on files : download inline
      {:href (str "/download?path=" (:path item) "&disposition=inline")
       :target (:name item)}))

(defn render-file [item-m]
  [:a {:href   (str "/download?path=" (:path item-m) "&disposition=inline")
       :target (:name item-m)}
   (:name item-m)])

(defn render-dir [item-m]
  [:a {:href (create-url-explore (:id item-m))}
   (:name item-m)])

(defn render-action-item [item-m action-m]
  [:li
   [:a {:href  ""
        :on-click (fn [event]
                    (cancel-event event)
                    (js/console.log (str "running command " (:name action-m)))
                    (>run-command (:name action-m) (:id item-m)))}
    (:name action-m)]])

(defn actions-for [item-m config]
  (when-let [item-type (:type item-m)]
    [:ul
     (->> config
          :types
          (filter #(= item-type (:name %)))
          first
          :actions
          (map #(render-action-item item-m %)))]))

(defn render-item-row [config item]
  (let [is-dir (:dir? item)]
    [:tr  {:key (:path item)}
     [:td {:width "40px"}
      (if is-dir
        folder-icon
        file-icon)]

     [:td
      (if is-dir
        [render-dir  item]
        [render-file item])]

     [:td (:type item)]
     [:td (actions-for item config)]]))

(defn explorer-view []
  (let [loading? (<loading?)]
    [:div
     (when-not loading?
       (let [list-items (<sorted-explore)
             config     (<config)]
         (tap> {:config config
                :list-items list-items})
         (if-not  (zero? (count list-items))
           [:table.table.is-hoverable.is-fullwidth
            [:tbody
             (map #(render-item-row config %) list-items)]]
           [:div [warning-message "This folder is empty"]])))]))

(defn toolbar []
  (let [path              (r/atom "")
        update-input-path #(reset! path (-> % .-target .-value))]
    (fn []
      [:div
       [:button
        {:on-click #(>navigate-to-explore @path)} "explore"]
       [:input {:type        "text"
                :placeholder "enter path"
                :value       @path
                :on-change   update-input-path}]])))

(defn breadcrumbs []
  (let [crumbs     (<breadcrumbs)]
    [:div.breadcrumb.has-arrow-separator.is-medium
     [:ul
      [:li {:key  "home"} [:a {:href (create-url-explore "")} home-icon]]

      (when (seq crumbs)
        (concat

         (when-let [head (butlast crumbs)]
           (->> head
                (map (fn [{:keys [name path]}]
                       [:li  {:key path}
                        [:a {:href (create-url-explore path)} name]]))))

         (when-let [last-crumb (last crumbs)]
           [[:li.is-active {:key  (:path last-crumb)}
             [:a {:href (create-url-explore (:path last-crumb))}
              [:div.tags [:span.tag.is-medium.is-info.has-text-weight-bold (:name last-crumb)]]]]])))]]))


(defn page [params]
  [:div
   [breadcrumbs]
   #_[toolbar]
   [explorer-view]])
