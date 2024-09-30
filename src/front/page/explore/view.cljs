(ns page.explore.view
  (:require [components.icon :refer [file-icon folder-icon home-icon]]
            [components.message :refer [warning-message]]
            [page.config.subs :refer [<config]]
            [page.explore.event :refer [>run-command]]
            [page.explore.subs :refer [<breadcrumbs <loading? <sorted-explore]]
            [reagent.core :as r]
            [route.helper :refer [>navigate-to-explore create-url-explore]]
            [utils :refer [cancel-event]]))

(defn render-file [item-m]
  [:a {:href   (str "/download?path=" (:path item-m) "&disposition=inline")
       :target (:name item-m)}
   (:name item-m)])

(defn render-dir [{:keys [id name] :as _item-m}]
  [:a {:href (create-url-explore id)}
   name])

(defn render-action-item [{item-id :id} {action-name :name}]
  [:li {:key action-name}
   [:a {:href      ""
        :on-click (fn [event]
                    (cancel-event event)
                    (js/console.log (str "running command " action-name))
                    (>run-command action-name item-id))}
    action-name]])

(defn actions-for [{:keys [type] :as item-m} config]
  (when type
    [:ul
     (->> config
          :types
          (filter #(= type (:name %)))
          first
          :actions
          (map #(render-action-item item-m %)))]))

(defn render-item-row [config {:keys [dir? path type] :as item}]
  [:tr  {:key path}
   [:td {:width "40px"}
    (if dir?
      folder-icon
      file-icon)]
   [:td
    (if dir?
      [render-dir  item]
      [render-file item])]
   [:td type]
   [:td (actions-for item config)]])

(defn explorer-view []
  (let [loading? (<loading?)]
    [:div
     (when-not loading?
       (let [list-items (<sorted-explore)
             config     (<config)]
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
